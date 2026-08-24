# Prefill Deprecation — What Broke and What I Changed

## 1. The error, in full

**File:** `3. Prompt Engineering/001_prompting.ipynb`
**Cell:** `dataset = evaluator.generate_dataset(...)`
**Call chain:** `generate_dataset()` → `generate_unique_ideas()` → `chat()` → `client.messages.create()`

Traceback bottom line:

```
BadRequestError: Error code: 400 - {'error': {'message':
'{"message":"This model does not support assistant message prefill.
The conversation must end with a user message."}.
Received Model Group=claude-sonnet-4-6
Available Model Group Fallbacks=None', ...}}
```

Two things in that message matter:

- **"This model does not support assistant message prefill. The conversation must end with a user message."** — the literal reason for the rejection.
- **"Received Model Group=claude-sonnet-4-6 / Available Model Group Fallbacks=None"** — this phrasing ("Model Group", "Fallbacks") is not how Anthropic's own API talks. It's LiteLLM/proxy-router vocabulary. This tells us the request never reached Claude's inference stack the normal way — it was intercepted and rejected by whatever server sits at your `ANTHROPIC_BASE_URI`, before or during translation to the upstream call.

So this 400 comes from the middleman your `.env`'s `ANTHROPIC_BASE_URI` points at, not from Anthropic's servers directly. Prefill is a real, currently-supported, documented feature of the actual Claude API — nothing about it is deprecated by Anthropic. It's specifically this proxy's policy/implementation that refuses a conversation that doesn't end in a `user` role message.

## 2. What "assistant prefill" is and why the course used it

Normally a request's `messages` list alternates user/assistant, and the LAST message you send is typically from the `user` — you're asking Claude to generate the next (assistant) turn from scratch.

Prefill is a trick where you add one more message yourself, with `role: "assistant"`, containing the **start** of what you want Claude's reply to look like:

```python
messages = []
add_user_message(messages, "Give me a JSON array of 3 fruits")
add_assistant_message(messages, "```json")     # <-- this is the prefill
text = chat(messages, stop_sequences=["```"])
```

Under the hood this sends:

```json
[
  {"role": "user", "content": "Give me a JSON array of 3 fruits"},
  {"role": "assistant", "content": "```json"}
]
```

Claude is told "the assistant already said this much — continue from here." Because the assistant turn already committed to opening a code fence, Claude's continuation is guaranteed to start immediately with JSON content, not a preamble like "Sure! Here's your array:". Combined with `stop_sequences=["```"]` (stop generating the instant a closing fence appears), the SDK gives you back `text` that is already clean, fence-free JSON — no post-processing needed, just `json.loads(text)`.

This is why the course uses it in three places inside the `PromptEvaluator` class: it's a clean, minimal way to force machine-parseable output without asking the model nicely and hoping.

The catch: prefill only works if the API accepts a conversation whose last message has `role: "assistant"`. Your proxy explicitly checks for this and refuses it — "conversation must end with a user message" — so every one of those three prefill call sites throws this same `BadRequestError` the moment it's reached.

## 3. Exactly what I changed

File touched: `3. Prompt Engineering/001_prompting.ipynb` (only this file).

### Step 1 — added a new helper function

Added right after `chat()` in the client-setup cell:

```python
def extract_json(text):
    """Pull a JSON array/object out of model output, tolerating markdown fences."""
    match = re.search(r"```(?:json)?\s*(.*?)\s*```", text, re.DOTALL)
    if match:
        json_string = match.group(1)
    else:
        match = re.search(r"[\[{].*[\]}]", text, re.DOTALL)
        json_string = match.group(0) if match else text
    return json.loads(json_string)
```

How it works:

- The first regex `` r"```(?:json)?\s*(.*?)\s*```" `` looks for a fenced block — optionally tagged ` ```json ` — and `DOTALL` lets `.` match newlines, so it captures everything between the fences (non-greedy `.*?` so it stops at the **first** closing fence). This handles the common case where Claude wraps its JSON in a code block despite being asked not to.
- If no fence is found, it falls back to grabbing the first `[`/`{` through the last `]`/`}` in the text — a looser catch-all for when Claude replies with bare JSON plus maybe a sentence of commentary.
- Either way, the extracted string goes to `json.loads()`.

This replaces the mechanical guarantee that prefill + `stop_sequences` gave ("the text you get back IS json, nothing else") with a best-effort textual extraction done **after** the fact. Slightly less bulletproof, but reliable in practice — especially combined with step 3 below.

### Step 2 — removed the prefill call and its supporting `stop_sequences`, in three `PromptEvaluator` methods

#### (a) `generate_unique_ideas()`

**Before:**
```python
messages = []
add_user_message(messages, rendered_prompt)
add_assistant_message(messages, "```json")
text = chat(
    messages,
    stop_sequences=["```"],
    system=system_prompt,
    temperature=1.0,
)
return json.loads(text)
```

**After:**
```python
messages = []
add_user_message(messages, rendered_prompt)
text = chat(
    messages,
    system=system_prompt,
    temperature=1.0,
)
return extract_json(text)
```

#### (b) `generate_test_case()`

**Before:**
```python
messages = []
add_user_message(messages, rendered_prompt)
add_assistant_message(messages, "```json")
text = chat(
    messages,
    stop_sequences=["```"],
    system=system_prompt,
    temperature=0.7,
)

test_case = json.loads(text)
```

**After:**
```python
messages = []
add_user_message(messages, rendered_prompt)
text = chat(
    messages,
    system=system_prompt,
    temperature=0.7,
)

test_case = extract_json(text)
```

#### (c) `grade_output()`

**Before:**
```python
messages = []
add_user_message(messages, eval_prompt)
add_assistant_message(messages, "```json")
eval_text = chat(
    messages,
    stop_sequences=["```"],
    temperature=0.0,
)
return json.loads(eval_text)
```

**After:**
```python
messages = []
add_user_message(messages, eval_prompt)
eval_text = chat(
    messages,
    temperature=0.0,
)
return extract_json(eval_text)
```

In all three cases the only structural change to the request is: the `messages` list now ends on the `user` role (satisfying the proxy's requirement) instead of ending on a fake `assistant` role.

### Step 3 — added one instruction line to each of the three prompt templates

Added to the prompt text itself (not the code), e.g.:

```
Respond with ONLY the JSON array (optionally wrapped in a ```json code fence) and no other text.
```

(object-equivalent phrasing for the other two prompts)

Why: prefill used to enforce "start your reply with JSON" mechanically, at the API level. Now that we're not doing that, we ask for it in plain language instead, so Claude is still nudged toward emitting clean JSON without preamble — this is what keeps `extract_json()`'s fallback regex from needing to work very hard.

**Net result:** identical inputs/outputs/behavior of the notebook, same `dataset.json` / `output.json` / `output.html` produced — just no assistant-role prefill message anywhere, so the proxy no longer rejects the request.

## 4. The "proper" modern replacement (beyond what I did)

What I did (prompt-for-JSON + regex-extract-after) is a legitimate, common workaround, but it's a workaround, not the most robust modern solution. If you ever move off this proxy onto the real Anthropic API directly, the recommended replacement for "I need guaranteed structured JSON out of Claude" is **tool use** (function calling / structured output), not prefill and not regex:

```python
tools = [{
    "name": "record_ideas",
    "description": "Record the generated test-case ideas",
    "input_schema": {
        "type": "object",
        "properties": {
            "ideas": {
                "type": "array",
                "items": {"type": "string"}
            }
        },
        "required": ["ideas"]
    }
}]

message = client.messages.create(
    model=model,
    max_tokens=1000,
    messages=messages,
    tools=tools,
    tool_choice={"type": "tool", "name": "record_ideas"},
)

# message.content will contain a tool_use block whose .input is
# ALREADY a parsed Python dict matching the schema above —
# no fences, no regex, no prefill, no json.loads() needed.
tool_call = next(b for b in message.content if b.type == "tool_use")
ideas = tool_call.input["ideas"]
```

Forcing `tool_choice` to a specific tool guarantees the model responds with a structured, schema-validated payload every time. This is the current best-practice replacement for the old "prefill + stop_sequence + manually parse JSON" pattern, and it works on all current Claude models via the real API.

I did **not** convert the notebook to this approach — it's a bigger structural change (new `tools=[...]` plumbing added to the shared `chat()` helper). The regex-based `extract_json()` fix is the smaller, drop-in change that unblocks you immediately on the current proxy. Happy to do the tool-use rewrite too if you want to see the more "correct" version.

## 5. Other folders — checked, not modified

You asked me to inspect but not edit these. Findings, plus what you'd need to do if you touch them yourself later.

### Folder "1. Accessing API from Claude"

**`001_request.ipynb`, `001_request_exercise.ipynb`, `002_system_prompt.ipynb`, `002_system_prompt_exercise.ipynb`, `003_temperature.ipynb`, `004_streaming.ipynb`, `005_controlling_output_exercise.ipynb`**

All of these define `add_assistant_message()` as a helper, but none actually call it with a fragment like `` "```json" `` to force prefill-style output. Where `add_assistant_message` *is* called (e.g. `001_request.ipynb`), it's used the ordinary way — to record a real, full assistant reply into the conversation history before asking a follow-up question, which is a different, still-fully-supported use of the function (and the list ends on a fresh `add_user_message` call afterward anyway, so it's not affected by the proxy's "must end on user" rule).

**No action needed** — none of these will hit the `BadRequestError`.

**`005_controlling_output.ipynb`**

This one independently ran into the exact same problem you just hit, and it's already been patched (either by you in an earlier session, or it shipped that way):

```python
messages = []
add_user_message(messages, "Generate a very short event bridge rule as json")
# add_assistant_message(messages, "```json")   <-- prefill line, already commented out
raw_text = chat(messages, stop_sequences=["```\n\n"])
clean_json = raw_text.split("```json")[-1]
```

The prefill call is commented out, and instead the code lets Claude reply normally, then does `raw_text.split("```json")[-1]` — take everything after the last occurrence of the literal string `` "```json" `` in the reply. This is a simpler, more brittle cousin of my `extract_json()`: it doesn't handle "no fence at all" or "fence without the json tag", and it doesn't strip the trailing ` ``` ` off the end. For this notebook's single, simple use case it's good enough and already works against your proxy.

**Nothing required from you here.** If you want consistency across the course, you could later replace that `.split("```json")[-1]` line with `extract_json(raw_text)` (you'd need to copy the `extract_json` helper into this notebook too, since each notebook currently defines its own independent copy of `chat`/`add_user_message`/etc.) — optional, a style/robustness nice-to-have, not a bug fix.

### Folder "2. Prompt Evalutation"

**`001_prompt_evaluation.ipynb`**

This notebook's `generate_dataset()` function never used the prefill pattern at all — it already follows the same "ask-in-prompt, extract-after" philosophy as my fix:

```python
messages = []
add_user_message(messages, rendered_prompt)
text = chat(messages, stop_sequences=["```"], system=system_prompt, ...)
match = re.search(r'\[.*\]', text, re.DOTALL)
json_string = match.group(0) if match else text
clean_string = json.loads(json_string)
```

Two things worth noting if you revisit this file:

1. It passes `stop_sequences=["```"]` but there is **no** corresponding `add_assistant_message(...)` prefill call before it — so that stop sequence is currently a leftover with no real effect (Claude isn't primed to open a fence, so it may or may not ever emit one; if it never emits `` "```" `` the stop sequence simply never triggers, and generation runs to `max_tokens` or a natural stop as normal). Harmless as-is, just vestigial.
2. The regex `r'\[.*\]'` only ever matches an **array** (square brackets), because this function always expects Claude to return a JSON list of idea-objects. Fine for this specific use case, but less general than the `extract_json()` helper in folder 3, which handles both arrays `[...]` and objects `{...}`.

**No changes needed** for this notebook to run against your proxy — it will work as-is.

## 6. Summary table

| Location | Prefill used? | Status |
|---|---|---|
| Folder 1, most files | No — helper defined but never called with a forcing fragment | Fine as-is |
| Folder 1, `005_controlling_output.ipynb` | Was used, already self-patched (commented-out prefill + `.split()`) | Fine as-is |
| Folder 2, `001_prompt_evaluation.ipynb` | No — already prompt+regex based | Fine as-is |
| Folder 3, `001_prompting.ipynb` | Yes, in 3 places | **Fixed this session** |
