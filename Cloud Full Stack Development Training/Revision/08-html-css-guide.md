# HTML & CSS — Complete Line-by-Line Guide

This guide is grounded strictly in your actual course materials: the 12 HTML courseware modules and 10 CSS courseware modules (`Courseware/05-html-css-js-node-etc/01-html/` and `02-css/`), plus the real HTML/CSS files used in the Employee Management System (EMS) project (`Code/UI (HTML, CSS, JS, Ts, Node)/html/` and `Code/Express/ems-ui/`). Sections 2 and 3 summarize the courseware's concepts and mechanics for assessment prep. Section 4 walks through the actual project files line by line.

---

# PART 1 — HTML COURSEWARE

## 1. Introduction to HTML (Module 01)

- HTML = HyperText Markup Language, invented by Tim Berners-Lee at CERN (1989/90). It is a **markup language**, not a programming language — it annotates content with tags for structure/meaning; CSS handles presentation, JS handles behavior. Keeping these separate is a core professional principle.
- History: HTML 1.0 (1991, 18 elements) → HTML 2.0 (1995, RFC 1866) → HTML 3.2/4.0 (1997, tables/frames/scripting) → HTML 4.01 (1999) → XHTML 1.0 (2000, strict XML well-formedness) → HTML5 draft (2008, WHATWG) → HTML5 W3C recommendation (2014) → WHATWG Living Standard (2019, continuously updated, no more version numbers).
  - XHTML habits worth keeping: close every tag, lowercase attributes, quote all values.
- **Critical Rendering Path** (order matters for assessment questions): DNS resolution → TCP/TLS handshake → HTTP request/response → **HTML parsing → DOM** → **CSS parsing → CSSOM** → **Render Tree** (DOM + CSSOM, visible nodes only) → **Layout/Reflow** (sizes & positions) → **Paint** (pixels) → **Composite** (final image).
  - Render-blocking resources (`<script>` in `<head>` without `defer`/`async`) delay DOM construction.
- "Valid HTML" = passes the W3C Markup Validator; catches unclosed tags, wrong nesting, ensures accessibility tooling works.
- **Always declare `<meta charset="UTF-8">`** — must be first, supports all Unicode, avoids locale bugs.

## 2. Tags, Attributes & Elements (Module 02)

- **Element** = opening tag + content + closing tag. **Tag** = just the `<name>` delimiter. Tags are case-insensitive but lowercase is convention.
- **Void (self-closing) elements** have no closing tag: `area, base, br, col, embed, hr, img, input, link, meta, param, source, track, wbr`.
- **Attribute syntax rules**: lowercase names, double-quoted values, no spaces around `=`. **Boolean attributes** (`required`, `disabled`, `checked`) need no value — presence alone means true.
- **Global attributes** valid on every element: `id`, `class`, `style`, `title`, `lang`, `dir`, `hidden`, `tabindex`, `data-*`, `aria-*`, `contenteditable`, `draggable`.
- `data-*` attributes pass arbitrary data to JavaScript; read via `element.dataset.camelCaseName` (kebab-case auto-converts to camelCase).
- **DOM tree terminology** (used directly in CSS selectors and JS traversal): parent, child, descendant, ancestor, sibling.
- **Content categories** define valid nesting: flow, phrasing (inline), heading, sectioning (`article/aside/nav/section`), embedded, interactive, metadata. Common mistakes: block element inside inline element; `<div>` inside `<p>`; interactive elements nested inside each other (e.g., `<button>` inside `<a>`).
- **HTML comments** (`<!-- -->`) are visible in page source — never put secrets in them.
- **HTML entities** must escape special characters: `&lt;` `&gt;` `&amp;` `&quot;` `&apos;` `&nbsp;` `&copy;` etc.

## 3. Tools & Setup (Module 03)

- Recommended editors: VS Code (with HTML CSS Support, Auto Rename Tag, Prettier, Live Server extensions) or IntelliJ IDEA/WebStorm.
- **Emmet** shorthand expansion (built into VS Code/IntelliJ): e.g. `ul>li*5` → `<ul>` with 5 `<li>`; `!` → full HTML5 boilerplate; `.card>.card-body>h3+p` → nested divs.
- **Browser DevTools** (F12): Elements panel shows the *live parsed DOM* (not raw source — "View Source" shows what the server sent, DevTools shows what the browser parsed, which JS may have modified). Use the selector tool to inspect box model, computed styles, and the Accessibility tab.
- Validators: **W3C Markup Validator** (validator.w3.org) catches missing attributes, invalid nesting, duplicate IDs; **axe DevTools** browser extension audits accessibility.
- `.browserslistrc` defines target browsers for Autoprefixer/Babel/PostCSS.

## 4. Document Structure (Module 04)

- **Full HTML5 boilerplate skeleton**: `<!DOCTYPE html>` → `<html lang="en">` → `<head>` (charset, viewport, description, favicon, stylesheets, title) → `<body>` (content, then scripts).
- `<!DOCTYPE html>` triggers **Standards Mode**; without it browsers use **Quirks Mode** (broken/old box model, unpredictable CSS) — always include it as the literal first line.
- `<html lang="en">` — the `lang` attribute is required for accessibility (screen reader pronunciation, spell-checkers, SEO, and enables CSS `:lang()`).
- `<head>` metadata rules:
  - `<meta charset="UTF-8">` must be the **first** element in `<head>`, within the first 1024 bytes.
  - `<meta name="viewport" content="width=device-width, initial-scale=1.0">` is essential for responsive design. **Never** use `user-scalable=no` or `maximum-scale=1` — fails WCAG 1.4.4 (Resize text).
  - `<title>` appears in tab, history, bookmarks, search results.
  - Stylesheets are **render-blocking** by design (prevents FOUC) — place in `<head>`.
  - Resource hints: `dns-prefetch`, `preconnect`, `prefetch`, `preload`.
- **Script loading strategies**: plain `<script>` in `<head>` blocks parsing (bad); `defer` downloads in parallel and executes after parsing, in order (preferred for app scripts); `async` downloads in parallel and executes immediately when ready, order not guaranteed (for independent scripts like analytics); or place scripts at end of `<body>`.
- Exactly one `<body>` per document.

## 5. Text Content (Module 05)

- **Headings** `<h1>`–`<h6>`: only one `<h1>` per page (main topic); never skip levels (screen readers navigate by heading level); headings define document outline, not visual size — use CSS for that.
- **Paragraphs** `<p>`: use separate `<p>` tags for spacing, not `<br><br>` and not empty `<p></p>` tags.
- `<br>` is for content-intrinsic line breaks only (postal address, poetry) — not visual spacing.
- `<hr>` represents a **thematic break** between sections, not just a visual line.
- **Semantic inline elements** and when to use them:
  - `<strong>` (semantic importance) vs `<b>` (stylistic bold only)
  - `<em>` (stressed emphasis, changes meaning) vs `<i>` (technical term/foreign phrase, no stress)
  - `<mark>` (highlighted/relevant text), `<small>` (side comments/legal text)
  - `<del>`/`<ins>` (tracked changes), `<sub>`/`<sup>` (subscript/superscript)
  - `<code>`, `<kbd>`, `<samp>`, `<var>` (technical documentation: inline code, keyboard input, sample output, variable names)
  - `<abbr title="...">` (abbreviation with hover tooltip)
  - `<cite>` (title of a work), `<q>` (inline quotation), `<blockquote>` (block quotation)
- `<address>` is for **contact information** near the nearest `<article>` or `<body>` — not generic postal addresses.
- `<pre>` preserves whitespace/line breaks, commonly paired with `<code>` for syntax-highlighted code blocks.
- HTML **collapses whitespace** (multiple spaces/tabs/newlines → single space); use `&nbsp;`, `<pre>`, or CSS `white-space` to control it.

## 6. Links & Navigation (Module 06)

- `<a href="...">` — `href` is the only required attribute.
- **URL types**: absolute (`https://...`), root-relative (`/dashboard` — preferred in apps), document-relative (fragile, avoid in server-rendered apps), same-page anchors (`href="#id"`).
- `target` values: `_self` (default), `_blank`, `_parent`, `_top`.
- **Security: always pair `target="_blank"` with `rel="noopener noreferrer"`** — prevents "reverse tabnapping" (the opened page accessing `window.opener`); `noreferrer` also strips the Referer header.
- Special `href` values: `mailto:`, `tel:`, `download="filename"` for forced downloads. **Avoid** `href="javascript:void(0)"` — use a `<button>` for actions instead of hijacking navigation semantics.
- **Link accessibility**: descriptive link text (never bare "click here"/"read more" without `aria-label` context) — screen readers often list all links out of context.
- **Skip navigation links**: first focusable element in `<body>`, visually hidden until `:focus` (positioned off-screen, then repositioned on focus).
- `<nav aria-label="...">` marks major navigation blocks; use `aria-label` to distinguish multiple `<nav>` regions; `aria-current="page"` on the active link.
- **Breadcrumbs** use `<nav aria-label="Breadcrumb"><ol>...</ol></nav>` — `<ol>` because breadcrumb order is meaningful.

## 7. Lists (Module 07)

- Three list types with distinct semantics: `<ul>` (unordered — order doesn't matter), `<ol>` (ordered — order matters), `<dl>` (description list — term/definition pairs).
- `<ol>` attributes: `type` (`1`, `a`, `A`, `i`, `I`), `start` (integer), `reversed` (boolean, counts down). Prefer CSS `list-style-type` for pure visual styling; use HTML attributes only when the value is semantically referenced.
- `<dl>` structure: `<dt>` (term) / `<dd>` (definition) — one `<dt>` can have multiple `<dd>`s and vice versa. Underused but ideal for metadata panels, glossaries, HTTP status code references.
- **Nesting rule**: a nested `<ul>`/`<ol>` must live **inside a `<li>`**, never directly inside the parent list.
- **Accessibility**: screen readers announce list type and item count ("List, 5 items"). Removing `list-style` with CSS strips list semantics in Safari — add `role="list"` to restore it. Navigation should always be `<ul><li>`, not `<div>` soup.

## 8. Images & Media (Module 08)

- `<img src="..." alt="...">` — `alt` is **required**; empty `alt=""` for purely decorative images (tells screen readers to skip), descriptive text for meaningful images (never just "chart" or "icon").
- Always specify `width` and `height` to prevent **Cumulative Layout Shift (CLS)** — browser reserves space before the image loads (works with `img { max-width: 100%; height: auto; }` for responsiveness).
- **Responsive images**:
  - `srcset` with density descriptors (`1x`, `2x`) or width descriptors (`400w`, `800w`) + `sizes` attribute — browser auto-selects the best image, no JS required.
  - `<picture>` with `<source>` elements for **art direction** (different crop per viewport) or **format switching** (AVIF/WebP with JPEG fallback).
  - Format guide: AVIF (best compression) > WebP (wide support) > SVG (vector, icons/logos) > PNG (transparency) > JPEG (photos) > GIF (legacy animation).
- **Lazy loading**: `loading="lazy"` for below-fold images; `loading="eager"` (default) + `fetchpriority="high"` for the LCP/hero image — **never lazy-load the hero image**.
- SVG: as `<img>` (simple, cacheable) or inline (stylable/animatable); decorative inline SVG gets `aria-hidden="true" focusable="false"`; meaningful standalone SVG needs `role="img"` + `<title>`.
- `<video>` with `controls`, `preload`, `poster`, `<source>` fallbacks, and `<track kind="subtitles">` for captions (legally required in many contexts, WCAG). `<audio>` similarly with `<source>` fallbacks.

## 9. Tables (Module 09)

- Tables are for **tabular data only** — never for page layout (CSS Grid/Flexbox replaced that role decades ago).
- Structure: `<table>` → `<caption>` → `<thead>`/`<tbody>`/`<tfoot>` → `<tr>` → `<th>`/`<td>`.
- **Accessibility**: `scope="col"` on column headers, `scope="row"` on row headers — tells screen readers which cells a header describes. `<caption>` is announced before the table content. For complex multi-level headers, use `id` on headers + `headers="id1 id2"` on data cells.
- **Spanning**: `colspan` (merge across columns), `rowspan` (merge across rows).
- `<colgroup>`/`<col>` apply column-level styling (limited CSS support: `width`, `border`, `background`, `visibility` only).
- **Responsive tables**: wrap in `<div class="table-wrapper" role="region" aria-label="..." tabindex="0">` with CSS `overflow-x: auto` — `tabindex="0"` makes it keyboard-scrollable.
- Sortable columns use `aria-sort` (`none`, `ascending`, `descending`, `other`) on `<th>`.

## 10. Forms (Module 10)

- `<form action="..." method="get|post" enctype="...">`. GET appends data to URL (bookmarkable — use for search/filter); POST sends in body (use for create/update/login/uploads). `enctype="multipart/form-data"` is **required** for file uploads.
- **Input types** and purpose: `text`, `email`, `password`, `search`, `url`, `tel` (text-like); `number`, `range` (numeric); `date`, `datetime-local`, `month`, `time`, `week` (date/time); `checkbox`, `radio` (selection — radio groups always in a `<fieldset>`); `file` (with `accept`, `multiple`), `hidden`, `color`.
- **Labels are mandatory** for accessibility: explicit (`<label for="id">` + matching `id`), implicit (input nested inside `<label>`), or `aria-label`/`aria-labelledby` when no visible label is possible.
- `<textarea>`, `<select>` (with `<option>`, `<optgroup>`, `multiple`+`size`).
- `<fieldset>` + `<legend>` groups related fields — **required** for radio/checkbox groups; screen readers announce the legend before each grouped input.
- **HTML5 validation attributes**: `required`, `minlength`/`maxlength`, `pattern` (regex), `min`/`max`/`step` (numeric). `novalidate` on `<form>` disables built-in validation for custom JS validation.
- CSRF protection concept (Thymeleaf/Spring context in courseware, but the mechanic generalizes): a hidden token field must accompany state-changing POST requests to prevent Cross-Site Request Forgery.

## 11. Semantic HTML5, IDs/Classes, Integration & Accessibility (Modules 11–14, combined file)

**Module 11 — Semantic layout elements:**
- Semantic HTML5 (`<header>`, `<footer>`, `<nav>`, `<main>`, `<article>`, `<section>`, `<aside>`) replaces "div soup" — benefits: screen reader landmark navigation, SEO structure understanding, self-documenting code.
- `<header>`/`<footer>` can appear at page level or nested inside `<article>` (article header/footer are scoped to that article).
- `<main>` — **exactly one per page**, holds primary content only (not header/nav/footer). Give it an `id` for skip-links.
- `<nav>` — major navigation blocks; multiple `<nav>`s distinguished by `aria-label`.
- `<section>` — thematic grouping **with a heading** (if no natural heading, use `<div>` instead).
- `<article>` — self-contained, syndicatable content (blog posts, comments, product cards).
- `<aside>` — tangentially related content (sidebars, "related reading").
- `<time datetime="ISO-format">` — machine-readable timestamps; screen readers and search engines parse `datetime`.
- Other HTML5 elements: `<figure>`/`<figcaption>` (image/code with caption), `<details>`/`<summary>` (JS-free accordion, `open` attribute expands by default), `<dialog>` (native modal, `.showModal()`/`.close()` JS API), `<progress>`/`<meter>` (determinate/indeterminate progress vs. scalar measurement).

**Module 12 — IDs, Classes, `data-*`:**
- `id` must be **unique per page**: used for CSS (sparingly — high specificity), JS `getElementById` (O(1) lookup), anchor links, `<label for="">`, ARIA references.
- `class` is reusable, space-separated, multiple per element.
- **BEM naming** (Block Element Modifier): `.card`, `.card__header` (double underscore = element), `.card__action--primary` (double hyphen = modifier). Widely used in enterprise CSS for predictable, low-specificity class names.
- `data-*` attributes pass server data to JS; `element.dataset.propertyName` auto-converts kebab-case to camelCase.

**Module 13 — Integration concepts** (Thymeleaf/Spring specifics in courseware; conceptually: template expressions for variables, conditionals, and fragment reuse — relevant to understanding server-rendered HTML generally, not required for the EMS static-file assessment).

**Module 14 — Performance, Accessibility & SEO:**
- **Core Web Vitals**: LCP (Largest Contentful Paint, target <2.5s), INP (Interaction to Next Paint, <200ms), CLS (Cumulative Layout Shift, <0.1). HTML techniques: `width`/`height` on images (CLS), `fetchpriority="high"` on LCP image, `loading="lazy"` below fold, `defer` on scripts.
- SEO essentials: unique `<title>`, `<meta name="description">`, `<link rel="canonical">`, Open Graph tags (`og:title`, `og:image`, etc.), JSON-LD structured data (`<script type="application/ld+json">`).
- **WCAG 2.1 AA checklist highlights** (assessment-relevant):
  - 1.1.1: all images have `alt`
  - 1.3.1: structure conveyed semantically (headings, lists, table `scope`)
  - 1.4.4: 200% zoom without content loss (no `user-scalable=no`)
  - 2.1.1: full keyboard operability (avoid `tabindex>0`, avoid `onclick` on non-interactive `<div>`s)
  - 2.4.1: skip navigation link
  - 2.4.2: descriptive page title
  - 3.1.1: `lang` attribute on `<html>`
  - 3.3.1: errors identified in text (`role="alert"`, `aria-invalid="true"`)
  - 4.1.2: accessible name for all UI components (`aria-label`, `aria-labelledby`)
- **Pre-production checklist** (paraphrased): DOCTYPE present; `lang` set; UTF-8 first in `<head>`; viewport meta present; unique title + description; all images have `alt` + dimensions; all inputs labeled; radio/checkbox groups in fieldsets; correct heading hierarchy (one `h1`, no skips); skip link present; valid HTML; ARIA landmarks present; no layout tables; scripts deferred/async; no sensitive data in source/comments.

---

# PART 2 — CSS COURSEWARE

## 1. Introduction & The Style Rule (Module 01)

- CSS = Cascading Style Sheets, proposed by Håkon Wium Lie (1994). "Cascading" refers to the conflict-resolution rules (specificity, source order, `!important`) used when multiple rules could apply to the same element.
- **Style rule anatomy**: `selector { property: value; }` — selector targets elements, declaration block holds property:value pairs (declarations), each ending in `;`.
- **Three ways to add CSS**:
  1. **Inline** (`style="..."`) — highest specificity, but no media queries/pseudo-classes, can't be cached separately, mixes concerns. Acceptable only for dynamically generated values.
  2. **Internal** (`<style>` in `<head>`) — no extra request, full CSS feature support, but not shared across pages (duplicated). Good for critical/above-the-fold CSS or single-page apps.
  3. **External** (`<link rel="stylesheet">`) — cacheable, clean separation, shared across pages. **The standard for enterprise apps.**
- Browsers apply a **user agent stylesheet** (default styles) before any author CSS — explains why `<h1>` is bold/large, `<ul>` has bullets, `<a>` is blue/underlined by default. Normalize.css or a custom reset smooths inconsistencies between browsers.
- **CSS processing order**: collect stylesheets → parse rules → resolve conflicts (cascade) → inherit → compute values → apply.

## 2. Selectors — Complete Reference (Module 02)

- **Basic selectors** and their specificity:
  - Type/element (`p`, `h1`) — specificity `0,0,1`
  - Class (`.btn`) — specificity `0,1,0`
  - ID (`#main`) — specificity `1,0,0` (avoid in shared stylesheets — nearly impossible to override without `!important`)
  - Universal (`*`) — specificity `0,0,0` (used for the `box-sizing: border-box` reset)
- **Attribute selectors**: `[required]` (has attribute), `[type="submit"]` (exact value), `[class~="btn"]` (contains word), `[href^="https"]` (starts with), `[href$=".pdf"]` (ends with), `[href*="external"]` (contains substring), `[lang|="en"]` (starts with or equals, hyphen-aware), `[type="TEXT" i]` (case-insensitive).
- **Pseudo-classes**:
  - Link states, in required order (**LVHA**): `:link`, `:visited`, `:hover`, `:active`.
  - Focus: `:focus`, `:focus-visible` (keyboard-only focus ring — never remove `:focus` styling without replacing it with a visible alternative).
  - Form states: `:disabled`, `:enabled`, `:checked`, `:required`, `:optional`, `:valid`, `:invalid`, `:user-invalid`, `:placeholder-shown`, `:read-only`, `:read-write`.
  - Structural: `:first-child`, `:last-child`, `:nth-child(n)` (supports `odd`, `even`, `3n`, `3n+1`), `:nth-last-child()`, `:first-of-type`/`:last-of-type` (counts only same-type siblings), `:not()`, `:only-child`.
  - Modern: `:is()`, `:where()` (like `:is()` but contributes zero specificity), `:has()` (parent selector — matches an element based on its descendants), `:target`, `:empty`.
- **Pseudo-elements**: `::before`/`::after` (virtual content insertion), `::first-line`/`::first-letter`, `::placeholder`, `::selection`, `::-webkit-scrollbar` family.
- **Combinators**:
  - Descendant (space): `.nav a` — any `<a>` anywhere inside `.nav`. Specificity = sum of both selectors.
  - Child (`>`): `ul > li` — only direct children.
  - Adjacent sibling (`+`): `h2 + p` — the `<p>` immediately following an `<h2>`.
  - General sibling (`~`): `h2 ~ p` — all `<p>`s that follow an `<h2>` sharing the same parent.
- **Grouping**: comma-separated selectors share one declaration block.
- **CSS Specificity — the key assessment mechanic**: think of it as a 3-column tuple (A=inline, B=ID, C=class/attribute/pseudo-class, D=element/pseudo-element — courseware groups it A,B,C where A=ID, B=class-level, C=element-level, plus inline as a separate highest tier).
  - Comparison rule: compare **left to right**; `1,0,0` (one ID) beats `0,99,99` (99 classes); `0,2,0` beats `0,1,10`.
  - **Equal specificity → last rule in source order wins.**
  - Example: `p{black}` (0,0,1) < `.intro{blue}` (0,1,0) < `p.intro{green}` (0,1,1) — green wins.
  - Avoid `!important` in shared/component stylesheets (breaks the override chain); acceptable only for utility classes (`.sr-only`) or overriding third-party CSS you can't edit.
  - Avoid chaining IDs/classes unnecessarily (`#sidebar .nav ul li a` = specificity 1,1,3 — nightmare to override); keep selectors flat (`.sidebar-link` = 0,1,0).
- Selector matching goes **right-to-left** (key selector is rightmost); rarely a real performance issue in modern browsers — prioritize readability.

## 3. The Cascade, Inheritance & Units (Module 03)

- **Cascade priority order** (highest to lowest): `!important` declarations → inline styles → ID selectors → class/attribute/pseudo-class selectors → element/pseudo-element selectors → inherited values → browser defaults.
- Equal specificity → **source order** decides (last rule wins) — this is why stylesheet `<link>` order matters (normalize.css before main.css before page-specific.css).
- **CSS Cascade Layers** (`@layer reset, base, components, utilities;`) give explicit override-order control independent of specificity/source order.
- **Inheritance**: some properties automatically flow parent → child.
  - **Inherited** (memorize for assessment): `color`, `font-family`, `font-size`, `font-weight`, `font-style`, `line-height`, `letter-spacing`, `text-align`, `text-transform`, `list-style`, `cursor`, `visibility`.
  - **Not inherited**: `margin`, `padding`, `border`, `background`, `width`, `height`, `display`, `position`, `top/right/bottom/left`, `float`, `overflow`, `box-shadow`, `border-radius`.
  - Controlling inheritance: `inherit` (force inherit a normally non-inherited property), `initial` (reset to spec default), `unset` (inherit if normally inherited, else initial), `all: initial|unset` (reset every property on a scoped component).
- **Computed value stages**: specified value (what you wrote, e.g. `1.5em`) → computed value (resolved, e.g. `24px`) → used value (post-layout) → actual value (post-rounding).
  - **`em` compounds** through nested elements (child's `1.5em` = 1.5× parent's computed size, grandchild compounds further) — this is *why `rem`* (relative to root `<html>` font-size only, no compounding) is preferred for font sizing.
- **Units**:
  - Absolute: `px` (logical, not physical pixel on retina), `pt`/`cm`/`mm`/`in` (print only).
  - Relative: `em` (parent font-size), `rem` (root font-size — no compounding, use for font sizes/spacing), `%` (parent dimension), `vw`/`vh` (1% of viewport width/height), `vmin`/`vmax`, `ch` (width of `0` character — good for form-field widths), `dvh` (dynamic viewport height, excludes mobile chrome).
  - Practical guide: `rem` for font sizes and spacing scale; `%`/`vw` for responsive widths; `em` for component-relative padding; `px` for borders/shadows (don't need to scale); `ch` for form field widths.
  - `clamp(min, ideal, max)` — fluid typography/spacing without media queries.
  - Logical properties (`margin-inline`, `padding-block`, etc.) adapt to writing direction — essential for RTL language support.
- **CSS Custom Properties (variables)**: defined on `:root` (e.g., `--color-primary: #0066cc;`), consumed via `var(--color-primary)`. Foundation of design systems and dark mode (`@media (prefers-color-scheme: dark) { :root { --color-bg: ...; } }` — components using `var()` update automatically, no duplication).

## 4. The Box Model, Borders & Backgrounds (Module 04)

- **The box model** (outside-in): margin → border → padding → content.
- **Two box-sizing models** — critical assessment mechanic:
  - `content-box` (default): `width` = content only; total rendered width = `width + padding + border` (e.g., 300px width + 20px padding + 2px border = **344px total**).
  - `border-box`: `width` = content + padding + border combined; total rendered width = exactly the specified `width` (padding/border eat into the 300px, staying **300px total**).
  - **Industry standard**: apply `*, *::before, *::after { box-sizing: border-box; }` globally — makes sizing intuitive.
- **Width/height**: `width`, `height`, `max-width`, `min-width`, `max-height`, `min-height`; intrinsic sizing keywords `fit-content`, `max-content`, `min-content`.
- **Margin**: outside the border. Shorthand order: all sides / vertical|horizontal / top|horizontal|bottom / top|right|bottom|left (clockwise from top). `margin-inline: auto` (or `margin: 0 auto`) centers a block element horizontally.
  - **Margin collapse**: adjacent vertical margins between block siblings collapse to `max()` of the two, not the sum (e.g., `p{margin-bottom:20px}` + `h2{margin-top:30px}` → 30px gap, not 50px). Does NOT collapse in flexbox/grid containers, floated elements, inline-block, or elements with non-visible `overflow`.
- **Padding**: inside the border, part of the background-painted area, **never collapses**, cannot be negative.
- **Borders**: shorthand `width style color`; individual sides/properties; `border-radius` (including `50%` for circles, `9999px` for pills).
- `outline` vs `border`: `outline` doesn't consume box-model space (good for focus rings) and can have `outline-offset`. **Never** `button:focus { outline: none; }` without a visible replacement (`box-shadow` ring) — accessibility failure.
- **Box-shadow**: `offset-x offset-y blur-radius spread-radius color`; `inset` keyword for inner shadows; multiple comma-separated shadows for layered depth.
- **Backgrounds**: `background-color`, `background-image: url()`, `background-size` (`cover` fills+crops, `contain` fits+may-gap), `background-position`, `background-repeat`; shorthand `background: color image position/size repeat;`. Gradients: `linear-gradient()`, `radial-gradient()`, `conic-gradient()`. Multiple layered backgrounds (first listed = topmost).
- **Overflow**: `visible` (default), `hidden` (clips + creates new block formatting context — also clips `border-radius` on children), `scroll`, `auto` (scrollbar only when needed); `overflow-x`/`overflow-y` for per-axis control.

## 5. Layout: Display, Float, Flexbox, Grid & Positioning (Module 05)

- **`display` values**: `block` (full width, new line — div/p/h1-h6/ul/li/table/form), `inline` (flows with text, ignores width/height/vertical margin — span/a/strong/em), `inline-block` (flows inline but respects width/height/margins), `flex`/`grid`/`inline-flex`/`inline-grid`, `none` (removed entirely, no space).
- Screen-reader-only pattern (`.sr-only`): visually hidden via absolute positioning + 1px clip, but still readable by assistive tech (different from `display:none` which hides from everyone).
- **Float/clear** (legacy, still valid for text-wrap-around-image): `float: left/right`; `clear: both/left/right`; clearfix hack (`::after { content:""; display:table; clear:both; }`) makes a parent contain floated children. Not for multi-column layout anymore — use Flexbox/Grid.
- **Flexbox — one-dimensional layout**:
  - Container: `display:flex`; `flex-direction` (row/row-reverse/column/column-reverse); `flex-wrap` (nowrap/wrap/wrap-reverse); `flex-flow` shorthand; **`justify-content`** (main-axis: flex-start/flex-end/center/space-between/space-around/space-evenly); **`align-items`** (cross-axis: stretch/flex-start/flex-end/center/baseline); `align-content` (multi-line cross-axis); `gap`/`row-gap`/`column-gap`.
  - Item: **`flex-grow`** (0=don't grow default, 1=take available space), **`flex-shrink`** (1=can shrink default, 0=fixed width), **`flex-basis`** (initial size before grow/shrink — `auto`, fixed px, or `0`); shorthand `flex: grow shrink basis` (e.g. `flex: 1` = `1 1 0`, `flex: none` = `0 0 auto`); `align-self` (override container's align-items for one item); `order` (default 0, negative moves earlier).
  - Common patterns: nav bar (`display:flex; align-items:center; gap`), push-right via `margin-left:auto` on last item, responsive card grid (`flex-wrap:wrap` + `flex: 1 1 280px`), centering (`justify-content:center; align-items:center; min-height:100vh`).
- **CSS Grid — two-dimensional layout**:
  - Container: `display:grid`; `grid-template-columns`/`grid-template-rows` (fixed px, `1fr` fractional units, `repeat(3, 1fr)`, `repeat(auto-fit, minmax(280px, 1fr))` for responsive columns); `grid-template-areas` (named ASCII-art layout); `gap`; `justify-items`/`align-items` (item alignment within cells); `justify-content`/`align-content` (track alignment within container).
  - Item placement: `grid-column`/`grid-row` (line numbers, e.g. `1 / 3`), `grid-column: 1 / -1` (span all columns), `grid-column: span 2`, named-area placement (`grid-area: header`), `justify-self`/`align-self` (override for one item).
  - Classic patterns: "Holy Grail" layout (header/sidebar/main/aside/footer via `grid-template-areas`), responsive product grid (`repeat(auto-fill, minmax(240px, 1fr))`), magazine layout (spanning columns/rows).
- **Positioning schemes** — assessment-relevant distinctions:
  - `static` (default): normal flow; `top/right/bottom/left`/`z-index` have no effect.
  - `relative`: stays in flow but can be offset via `top`/`left` etc.; **establishes a positioning context** for absolutely-positioned descendants.
  - `absolute`: removed from flow; positioned relative to the **nearest positioned ancestor** (any ancestor with `position` other than `static`) — falls back to the initial containing block if none exists. Used for tooltips, badges, overlays (`inset: 0` shorthand for `top/right/bottom/left: 0`).
  - `fixed`: positioned relative to the **viewport**, doesn't scroll with page (sticky headers, toasts).
  - `sticky`: scrolls normally until it crosses a threshold (e.g. `top: 0`), then sticks — used for table headers and sidebars.
  - `z-index` only affects **positioned** (non-static) elements; maintain a z-index scale via custom properties.
- **Media queries / responsive**: mobile-first approach (base styles for small screens, `min-width` queries enhance upward). Common breakpoints: 640/768/1024/1280/1536px. Also: `@media print`, `@media (prefers-reduced-motion: reduce)`, `@media (prefers-color-scheme: dark)`.

## 6. Typography (Module 06)

- **Font stacks**: system font stack (`system-ui, -apple-system, ...`) for performance/native feel; monospace stack for code; serif for long-form reading.
- `@font-face` for web fonts: `font-display: swap` shows fallback text immediately while the web font loads (prevents FOIT — Flash of Invisible Text).
- **Font size**: base `html { font-size: 16px; }`, then `rem`-based scale for headings (no compounding); `clamp()` for fluid typography without media queries. Type-scale ratios (Major Third 1.25, Perfect Fourth 1.333, etc.) create harmonious sizing.
- **Font weight**: numeric scale 100 (thin) to 900 (black), 400=normal, 700=bold.
- **Line height**: prefer **unitless values** (e.g. `1.6`) — inherits as a multiplier of each element's own font-size rather than a fixed px value, avoiding compounding issues on nested elements.
- **Letter-spacing**: tighter (negative em) for large headings, looser (positive em) for small caps/badges/uppercase labels.
- **Line length**: optimal readability ~45–75 characters; `max-width: 65ch` is the standard technique.
- **Text alignment**: use logical `text-align: start` (adapts LTR/RTL) over hardcoded `left`.
- **Truncation**: single-line ellipsis requires all three: `white-space: nowrap; overflow: hidden; text-overflow: ellipsis;` (plus a `max-width`). Multi-line clamp: `-webkit-line-clamp: N` + `display: -webkit-box; -webkit-box-orient: vertical; overflow: hidden;`.
- `overflow-wrap: break-word` (preferred) vs `word-break: break-all` for long unbreakable strings (URLs/IDs).

## 7. Styling Forms & Links (Module 07)

- Form controls have inconsistent native styling across browsers/OS — strategy: reset defaults, override what's controllable, use `appearance: none` for full custom control, accept native rendering for complex pickers (date, etc.).
- **Base reset**: `input, textarea, select, button { font-family: inherit; font-size: 1rem; color: inherit; margin: 0; box-sizing: border-box; }` — browsers do **not** inherit font-family into form controls by default, so this must be explicit.
- Standard input styling includes visible `:focus` state (border-color change + `box-shadow` ring) — **critical, always keep focus visible**; `:disabled` state should override the browser's default opacity dimming for consistency; `::placeholder` needs `opacity: 1` override in Firefox.
- Validation pseudo-classes for styling: `:user-invalid`/`:user-valid` (post-interaction) vs plain `:invalid`/`:valid` (immediate, can be premature).
- **Custom `<select>` styling**: `appearance: none` + a background-image SVG arrow + extra right padding, since native dropdown arrows can't be styled directly.
- **Custom checkboxes/radios**: two approaches — (1) visually hide the native input (clip-based, not `display:none` which breaks keyboard access) and style an adjacent `label::before` pseudo-element for the visual box/circle, toggling appearance via the `:checked` sibling selector; (2) the simpler modern `accent-color` property, which lets the browser render the native control in your brand color with full accessibility and cross-platform support but less design control.
- **Button styling**: reset `appearance`, define variants (`.btn-primary`, `.btn-secondary`/outline, `.btn-danger`, `.btn-ghost`) and sizes (`.btn-sm`, `.btn-lg`).
- **Link styling — must follow LVHA order** (`:link`, `:visited`, `:hover`, `:active`) or later rules won't apply correctly due to specificity/source-order interaction with pseudo-classes. Never remove underlines from body-text links (key accessibility affordance); navigation links commonly drop the underline since context/position already signals interactivity.
- Skip links: absolutely positioned off-screen (`left: -9999px`), repositioned to visible on `:focus`.

## 8. CSS Architecture (Module 08)

- **Normalize.css**: makes browser default styles *consistent* (not zeroed) across browsers — fixes heading sizes inside sectioning elements, form-control font inheritance, line-height quirks. Must load **before** your own stylesheet.
- Modern minimal reset alternative: `box-sizing: border-box` globally, `margin: 0` on `*`, `img/picture/video/svg { display:block; max-width:100%; }`, `input/button/textarea/select { font: inherit; }`.
- **7-1 file organization pattern**: `abstracts/` (variables/mixins, no CSS output) → `vendors/` (third-party) → `base/` (resets, typography) → `layout/` (header, footer, grid) → `components/` (buttons, cards, forms) → `pages/` (page-specific) → single `main.css` importing all in that order.
- **Design tokens via custom properties**: separate **primitive tokens** (raw values like `--blue-600: #2563eb;`) from **semantic tokens** that reference them (`--color-primary: var(--blue-600);`) — this indirection is what makes theme-switching (dark mode) trivial: swap only the semantic layer.
- **BEM component pattern**: `.card` (block), `.card__header`/`.card__body`/`.card__footer` (elements), `.card--elevated`/`.card--highlighted` (modifiers) — keeps specificity flat (always one class) and naming self-documenting.
- **Utility classes** (atomic CSS, Tailwind-inspired): single-purpose classes like `.flex`, `.items-center`, `.gap-4`, `.text-center`, `.rounded`.
- **Dark mode**: `@media (prefers-color-scheme: dark) { :root { /* override tokens */ } }`, or manual toggle via `[data-theme="dark"] { /* overrides */ }` — components referencing `var(--color-bg)` etc. update automatically with zero component-level changes.

## 9. Full Project Walkthrough (Module 09)

This courseware module builds an illustrative enterprise landing page (header/hero/news-events/footer) to demonstrate integrating everything above. Key patterns worth remembering for assessment:
- Sticky header: `position: sticky; top: 0; z-index: var(--z-sticky);`.
- Hero: CSS Grid, single column mobile → two columns desktop via a `min-width` media query; fluid heading via `clamp()`.
- Responsive card grid: `grid-template-columns: repeat(auto-fill, minmax(240px, 1fr))` — automatically adds/removes columns as viewport width changes, no explicit breakpoints needed for column count.
- "Clickable card" pattern: `.card { position: relative; }` + `.card__title a::after { content:""; position:absolute; inset:0; }` — makes the entire card clickable via one enlarged invisible link overlay, while keeping only one real anchor in the markup (good for accessibility — avoids duplicate/nested links).
- Footer: CSS Grid with responsive column counts (1 → 2 → 5 across breakpoints).
- Mobile nav toggle needs only minimal JS: toggling a `data-nav-open="true/false"` attribute that CSS attribute-selectors respond to — no class-toggling library required.
- Project-wide quality-gate checklist reinforces: `box-sizing: border-box` globally, Normalize loaded first, design tokens as custom properties, no `!important` in components, `:focus-visible` everywhere, mobile-first queries, tested at 320px and 1440px, reduced-motion respected, WCAG AA contrast ratios (4.5:1 normal text, 3:1 large text/UI).

---

# PART 3 — REAL PROJECT WALKTHROUGH (Line by Line)

This section reproduces the four actual project files exactly as they exist on disk and explains each meaningful line.

## A. `Code/UI (HTML, CSS, JS, Ts, Node)/html/index.html`

```html
1:  <!DOCTYPE html>
2:  
3:  <html>
4:  
5:  <head>
6:      <script src='main.js'></script>
7:  </head>
8:  
9:  <body>
10:     <h1 id="heading" style="color: blue;">HTML Demo</h1>
11:     <p>This is an HTML document.</p>
12:     <input type="text" required />
13:     <hr />
14:     <p></p>
15:     <hr>
16:     <p></p>
17: </body>
18: 
19: </html>
```

- **Line 1** — `<!DOCTYPE html>` triggers Standards Mode. Per the courseware (Module 04), this must be the literal first line or the browser falls back to Quirks Mode with a broken, non-spec box model.
- **Line 3** — `<html>` with **no `lang` attribute**. This is a courseware violation worth flagging on assessment: Module 04 states `lang` is required for accessibility (screen-reader pronunciation) and Module 14's pre-production checklist explicitly lists "lang attribute on `<html>`" as a WCAG 3.1.1 requirement. This file omits it.
- **Line 5–7** — `<head>` contains only a `<script src='main.js'>` tag with **no `defer`/`async`**, and notably **no `<meta charset="UTF-8">`**. Per Module 04 §4.4.1, charset must be the first element in `<head>` — this file skips it entirely, and per Module 04 §4.5, an un-deferred script in `<head>` is render-blocking, forcing the parser to stop, fetch, and execute `main.js` before it can continue parsing `<body>`. Single-quoted attribute value (`src='main.js'`) also deviates from the double-quote convention taught in Module 02 §2.3 (valid HTML, but not the taught convention).
- **Line 10** — `<h1 id="heading" style="color: blue;">` combines an `id` (unique identifier, per Module 12 usable for CSS/JS targeting or anchor links) with an **inline `style` attribute**. Per CSS Module 01 §1.3, inline styles are the least maintainable option — acceptable only for dynamically generated values, not general styling; this is a simple demo shortcut, not the recommended production pattern.
- **Line 11** — Plain `<p>` paragraph, correctly used per Module 05 §5.2.
- **Line 12** — `<input type="text" required />` is a **void element** (Module 02 §2.2) with the optional XHTML-style self-closing slash (harmless in HTML5, a holdover convention). `required` is a **boolean attribute** — its bare presence (no `="required"` needed) makes the field mandatory per Module 02 §2.3 and Module 10 §10.8. Note this input has **no associated `<label>`** — a WCAG/Module 10 §10.4 violation ("every input must have a label").
- **Line 13, 15** — `<hr />` / `<hr>` — thematic break elements (Module 05 §5.2), shown here in both self-closing and non-self-closing form to illustrate that HTML5 treats them identically.
- **Line 14, 16** — `<p></p>` — **empty paragraph tags used for spacing**. Module 05 §5.2 explicitly calls this out as a common mistake ("❌ Empty `<p>` tags for spacing... ✅ Use CSS margin/padding instead"). This file demonstrates the anti-pattern directly.

## B. `Code/UI (HTML, CSS, JS, Ts, Node)/html/create-employee.html`

```html
1:  <!DOCTYPE html>
2:  <html lang="en">
3:  
4:  <head>
5:      <meta charset="UTF-8">
6:      <meta name="viewport" content="width=device-width, initial-scale=1.0">
7:      <title>New Employee Record</title>
8:      <!--
9:      -->
10:     <style>
11:         .my-style {
12:             color: blue;
13:         }
14:  
15:         #my-style {
16:             color: yellow;
17:         }
18:  
19:         body {
20:             font-family: Arial, sans-serif;
21:             font-size: 15px;
22:             background-color: #f0f2f5;
23:             color: #222;
24:             margin: 0;
25:             padding: 40px 20px;
26:         }
27:  
28:         h1 {
29:             text-align: center;
30:             font-size: 26px;
31:             font-weight: 700;
32:             color: #1a1a2e;
33:             margin-bottom: 30px;
34:             letter-spacing: 0.5px;
35:         }
36:  
37:         form {
38:             max-width: 700px;
39:             margin: 0 auto;
40:             background-color: #ffffff;
41:             padding: 36px 40px;
42:             border-radius: 10px;
43:             box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
44:         }
45:  
46:         fieldset {
47:             border: 1.5px solid #c8d0dc;
48:             border-radius: 8px;
49:             padding: 20px 24px;
50:             margin-bottom: 24px;
51:             background-color: #fafbfc;
52:         }
53:  
54:         legend {
55:             font-size: 14px;
56:             font-weight: 700;
57:             color: #ffffff;
58:             background-color: #2c5f8a;
59:             padding: 4px 14px;
60:             border-radius: 20px;
61:             letter-spacing: 0.4px;
62:         }
63:  
64:         label {
65:             display: inline-block;
66:             font-size: 13px;
67:             font-weight: 600;
68:             color: #444;
69:             margin-bottom: 4px;
70:             margin-top: 10px;
71:         }
72:  
73:         input[type="text"],
74:         input[type="email"],
75:         input[type="tel"],
76:         input[type="number"],
77:         input[type="date"],
78:         input[type="time"],
79:         input[type="month"],
80:         input[type="week"],
81:         input[type="url"],
82:         input[type="search"] {
83:             display: block;
84:             width: 100%;
85:             box-sizing: border-box;
86:             padding: 9px 12px;
87:             font-size: 14px;
88:             border: 1.5px solid #c8d0dc;
89:             border-radius: 6px;
90:             background-color: #ffffff;
91:             color: #222;
92:             outline: none;
93:             margin-bottom: 4px;
94:             transition: border-color 0.2s;
95:         }
96:  
97:         input[type="text"]:focus,
98:         input[type="email"]:focus,
99:         input[type="tel"]:focus,
100:        input[type="number"]:focus,
101:        input[type="date"]:focus,
102:        input[type="time"]:focus,
103:        input[type="month"]:focus,
104:        input[type="week"]:focus,
105:        input[type="url"]:focus,
106:        input[type="search"]:focus {
107:            border-color: #2c5f8a;
108:            box-shadow: 0 0 0 3px rgba(44, 95, 138, 0.15);
109:        }
110: 
111:        select {
112:            display: block;
113:            width: 100%;
114:            box-sizing: border-box;
115:            padding: 9px 12px;
116:            font-size: 14px;
117:            border: 1.5px solid #c8d0dc;
118:            border-radius: 6px;
119:            background-color: #ffffff;
120:            color: #222;
121:            outline: none;
122:            cursor: pointer;
123:        }
124: 
125:        select:focus {
126:            border-color: #2c5f8a;
127:            box-shadow: 0 0 0 3px rgba(44, 95, 138, 0.15);
128:        }
129: 
130:        textarea {
131:            display: block;
132:            width: 100%;
133:            box-sizing: border-box;
134:            padding: 9px 12px;
135:            font-size: 14px;
136:            font-family: Arial, sans-serif;
137:            border: 1.5px solid #c8d0dc;
138:            border-radius: 6px;
139:            background-color: #ffffff;
140:            color: #222;
141:            outline: none;
142:            resize: vertical;
143:            margin-bottom: 4px;
144:        }
145: 
146:        textarea:focus {
147:            border-color: #2c5f8a;
148:            box-shadow: 0 0 0 3px rgba(44, 95, 138, 0.15);
149:        }
150: 
151:        input[type="range"] {
152:            display: block;
153:            width: 100%;
154:            margin: 8px 0 4px;
155:            accent-color: #2c5f8a;
156:            cursor: pointer;
157:        }
158: 
159:        input[type="color"] {
160:            display: block;
161:            width: 60px;
162:            height: 36px;
163:            border: 1.5px solid #c8d0dc;
164:            border-radius: 6px;
165:            padding: 2px;
166:            cursor: pointer;
167:            background-color: #fff;
168:        }
169: 
170:        input[type="file"] {
171:            display: block;
172:            font-size: 13px;
173:            color: #555;
174:            margin-bottom: 4px;
175:            cursor: pointer;
176:        }
177: 
178:        input[type="radio"],
179:        input[type="checkbox"] {
180:            accent-color: #2c5f8a;
181:            width: 15px;
182:            height: 15px;
183:            cursor: pointer;
184:            vertical-align: middle;
185:            margin-right: 4px;
186:        }
187: 
188:        output {
189:            font-weight: 700;
190:            color: #2c5f8a;
191:        }
192: 
193:        input[type="submit"] {
194:            background-color: #2c5f8a;
195:            color: #ffffff;
196:            border: none;
197:            padding: 10px 24px;
198:            font-size: 14px;
199:            font-weight: 600;
200:            border-radius: 6px;
201:            cursor: pointer;
202:            transition: background-color 0.2s;
203:        }
204: 
205:        input[type="submit"]:hover {
206:            background-color: #1e4468;
207:        }
208: 
209:        input[type="reset"] {
210:            background-color: #e8edf2;
211:            color: #333;
212:            border: 1.5px solid #c8d0dc;
213:            padding: 10px 24px;
214:            font-size: 14px;
215:            font-weight: 600;
216:            border-radius: 6px;
217:            cursor: pointer;
218:            margin-left: 10px;
219:            transition: background-color 0.2s;
220:        }
221: 
222:        input[type="reset"]:hover {
223:            background-color: #d5dce5;
224:        }
225: 
226:        button {
227:            background-color: #ffffff;
228:            color: #2c5f8a;
229:            border: 1.5px solid #2c5f8a;
230:            padding: 10px 24px;
231:            font-size: 14px;
232:            font-weight: 600;
233:            border-radius: 6px;
234:            cursor: pointer;
235:            margin-left: 10px;
236:            transition: background-color 0.2s, color 0.2s;
237:        }
238: 
239:        button:hover {
240:            background-color: #2c5f8a;
241:            color: #ffffff;
242:        }
243: 
244:        br {
245:            display: block;
246:            margin: 2px 0;
247:        }
248:    </style>
249: 
250: </head>
251: 
252: <body>
253: 
254:    <h1>New Employee Record</h1>
255: 
256:    <form action="/employees" method="post" enctype="multipart/form-data">
257: 
258:        <fieldset>
259:            <legend>Personal Information</legend>
260: 
261:            <label for="first_name">First Name:</label><br>
262:            <input type="text" id="first_name" name="first_name" placeholder="e.g. Shridhar" autocomplete="given-name"
263:                required><br><br>
264: 
265:            <label for="last_name">Last Name:</label><br>
266:            <input type="text" id="last_name" name="last_name" placeholder="e.g. Kumar" autocomplete="family-name"
267:                required><br><br>
268: 
269:            <label for="email">Email:</label><br>
270:            <input type="email" id="email" name="email" placeholder="employee@company.com" autocomplete="email"
271:                required><br><br>
272: 
273:            <label for="phone">Phone:</label><br>
274:            <input type="tel" id="phone" name="phone" placeholder="+91 98765 43210" autocomplete="tel"><br><br>
275: 
276:            <label for="dob">Date of Birth:</label><br>
277:            <input type="date" id="dob" name="dob"><br><br>
278: 
279:            <label>Gender:</label><br>
280:            <input type="radio" id="male" name="gender" value="male">
281:            <label for="male">Male</label>
282:            <input type="radio" id="female" name="gender" value="female">
283:            <label for="female">Female</label>
284:            <input type="radio" id="other" name="gender" value="other">
285:            <label for="other">Other</label>
286:            <input type="radio" id="prefer_not" name="gender" value="prefer_not">
287:            <label for="prefer_not">Prefer not to say</label><br><br>
288: 
289:            <label for="profile_photo">Profile Photo:</label><br>
290:            <input type="file" id="profile_photo" name="profile_photo" accept="image/*"><br><br>
291: 
292:        </fieldset>
293: 
294:        <br>
295: 
296:        <fieldset>
297:            <legend>Job Details</legend>
298: 
299:            <label for="employee_id">Employee ID:</label><br>
300:            <input type="number" id="employee_id" name="employee_id" placeholder="e.g. 10042" min="1" step="1"><br><br>
301: 
302:            <label for="join_date">Joining Date:</label><br>
303:            <input type="date" id="join_date" name="join_date"><br><br>
304: 
305:            <label for="department">Department:</label><br>
306:            <select id="department" name="department">
307:                <option value="">-- Select Department --</option>
308:                <option value="engineering">Engineering</option>
309:                <option value="hr">Human Resources</option>
310:                <option value="finance">Finance</option>
311:                <option value="marketing">Marketing</option>
312:                <option value="operations">Operations</option>
313:                <option value="sales">Sales</option>
314:            </select><br><br>
315: 
316:            <label for="job_title">Job Title:</label><br>
317:            <input type="text" id="job_title" name="job_title" list="job_titles_list"
318:                placeholder="Search or type title">
319:            <datalist id="job_titles_list">
320:                <option value="Software Engineer">
321:                <option value="Senior Software Engineer">
322:                <option value="Tech Lead">
323:                <option value="Project Manager">
324:                <option value="Business Analyst">
325:                <option value="HR Manager">
326:                <option value="DevOps Engineer">
327:            </datalist><br><br>
328: 
329:            <label for="employment_type">Employment Type (select one):</label><br>
330:            <select id="employment_type" name="employment_type" size="4">
331:                <option value="fulltime">Full-time</option>
332:                <option value="parttime">Part-time</option>
333:                <option value="contract">Contract</option>
334:                <option value="intern">Intern</option>
335:            </select><br><br>
336: 
337:            <label for="work_shift">Work Shift Start Time:</label><br>
338:            <input type="time" id="work_shift" name="work_shift"><br><br>
339: 
340:            <label for="salary">Annual Salary (₹): <output id="salary_output">500000</output></label><br>
341:            <input type="range" id="salary" name="salary" min="300000" max="5000000" step="50000" value="500000"
342:                oninput="document.getElementById('salary_output').value = this.value"><br><br>
343: 
344:            <label>Skills:</label><br>
345:            <input type="checkbox" id="skill_java" name="skills" value="java">
346:            <label for="skill_java">Java</label>
347:            <input type="checkbox" id="skill_spring" name="skills" value="spring">
348:            <label for="skill_spring">Spring Boot</label>
349:            <input type="checkbox" id="skill_mongodb" name="skills" value="mongodb">
350:            <label for="skill_mongodb">MongoDB</label>
351:            <input type="checkbox" id="skill_react" name="skills" value="react">
352:            <label for="skill_react">React</label>
353:            <input type="checkbox" id="skill_python" name="skills" value="python">
354:            <label for="skill_python">Python</label>
355:            <input type="checkbox" id="skill_devops" name="skills" value="devops">
356:            <label for="skill_devops">DevOps</label><br><br>
357: 
358:        </fieldset>
359: 
360:        <br>
361: 
362:        <fieldset>
363:            <legend>Additional Information</legend>
364: 
365:            <label for="website">LinkedIn / Portfolio URL:</label><br>
366:            <input type="url" id="website" name="website" placeholder="https://linkedin.com/in/..."><br><br>
367: 
368:            <label for="probation_end">Probation End Month:</label><br>
369:            <input type="month" id="probation_end" name="probation_end"><br><br>
370: 
371:            <label for="review_week">Performance Review Week:</label><br>
372:            <input type="week" id="review_week" name="review_week"><br><br>
373: 
374:            <label for="badge_color">Badge Accent Color:</label><br>
375:            <input type="color" id="badge_color" name="badge_color" value="#0011fa"><br><br>
376: 
377:            <label for="search_manager">Search Reporting Manager:</label><br>
378:            <input type="search" id="search_manager" name="search_manager" placeholder="Search by name..."><br><br>
379: 
380:            <label for="resume">Resume / Documents (multiple allowed):</label><br>
381:            <input type="file" id="resume" name="resume" accept=".pdf,.doc,.docx" multiple><br><br>
382: 
383:            <input type="hidden" id="dept_id" name="dept_id" value="DEPT-0042">
384: 
385:            <label for="notes">Additional Notes:</label><br>
386:            <textarea id="notes" name="notes" rows="4" cols="50"
387:                placeholder="Any remarks about the employee..."></textarea><br><br>
388: 
389:        </fieldset>
390: 
391:        <br>
392:        <fieldset>
393:            <legend>Consent &amp; Confirmation</legend>
394: 
395:            <input type="checkbox" id="consent_data" name="consent_data" value="yes" required>
396:            <label for="consent_data">I confirm the information is accurate and consent to data
397:                processing.</label><br><br>
398: 
399:            <input type="checkbox" id="consent_policy" name="consent_policy" value="yes">
400:            <label for="consent_policy">The employee has acknowledged the company's code of conduct.</label><br><br>
401: 
402:        </fieldset>
403: 
404:        <br>
405: 
406:        <input type="submit" value="Create Employee">
407:        <input type="reset" value="Reset Form">
408:        <button type="button" onclick="alert('Draft saved!')">Save as Draft</button>
409: 
410:    </form>
411: 
412: </body>
413: 
414: </html>
```

### Document head (lines 1–250)

- **Line 1–2** — `<!DOCTYPE html>` + `<html lang="en">`. Unlike file A, this file correctly sets `lang="en"` per Module 04 §4.3.
- **Line 5–6** — `<meta charset="UTF-8">` correctly placed as the first element in `<head>` (Module 04 §4.4.1), followed by the responsive viewport meta tag (Module 04 §4.4.2) with `width=device-width, initial-scale=1.0` and no `user-scalable=no` — compliant with the accessibility warning in the courseware.
- **Line 7** — `<title>New Employee Record</title>` — descriptive per-page title (Module 04 §4.4.4 / WCAG 2.4.2).
- **Line 8–9** — An empty HTML comment `<!-- -->` spanning two lines. Harmless but purposeless; illustrates comment syntax from Module 02 §2.7.
- **Line 10–248** — An **internal `<style>` block** in `<head>` (CSS Module 01 §1.3's "Method 2: Internal Styles"). Appropriate here since this is a single, self-contained demo page — the courseware notes internal styles are reasonable "where there's only one HTML file."
  - **Line 11–13** — `.my-style { color: blue; }` — a class selector, specificity `0,1,0` (CSS Module 02 §2.2). Defined but **never referenced** in the body markup below — a dead rule, illustrating that class selectors alone don't apply anything unless used.
  - **Line 15–17** — `#my-style { color: yellow; }` — an ID selector, specificity `1,0,0` (CSS Module 02 §2.2), also unused in the markup. The pairing of `.my-style`/`#my-style` on consecutive lines appears to be a specificity-teaching artifact (ID beats class if both applied to one element) rather than functional styling.
  - **Line 19–26** — `body` selector (type selector, specificity `0,0,1`) sets the base font-family/size/colors and resets `margin: 0` while adding `padding: 40px 20px` — this establishes the page's typography which cascades (inherits) down to all text-containing descendants per CSS Module 03 §3.2 (`font-family`, `color`, `font-size` are all inherited properties).
  - **Line 28–35** — `h1` styling: centered, large, bold heading — purely visual, matches Module 05 (HTML) guidance that headings' semantic meaning is separate from their visual size (visual size is CSS's job).
  - **Line 37–44** — `form` selector: constrains form width to `max-width: 700px`, centers it with `margin: 0 auto` (CSS Module 04 §4.3 centering technique), and gives it a card-like appearance (`border-radius`, `box-shadow`) — a common enterprise form-container pattern.
  - **Line 46–52** — `fieldset` styling: bordered, rounded, padded container — visually groups the `<fieldset>` elements used throughout the body to separate "Personal Information," "Job Details," etc. (HTML Module 10 §10.7).
  - **Line 54–62** — `legend` styling: turns the plain-text `<legend>` into a pill-shaped colored badge (`background-color` + `border-radius: 20px`) — a common technique to visually elevate the otherwise plain default `<legend>` rendering.
  - **Line 64–71** — `label` styling: `display: inline-block` lets margins apply to what is normally an inline element (CSS Module 05 §5.1 — inline elements ignore vertical margins by default, so `inline-block` is required here for `margin-top`/`margin-bottom` to take effect).
  - **Line 73–95** — A grouped selector (CSS Module 02 §2.7) listing ten `input[type="..."]` **attribute selectors** (Module 02 §2.3) comma-separated, applying one shared declaration block to all text-like inputs. Sets `box-sizing: border-box` **per-component** (rather than globally via `*`) — this file does not have the universal `box-sizing: border-box` reset from CSS Module 04 §4.1, applying it selectively to these inputs, `select`, and `textarea` instead.
  - **Line 97–109** — The matching `:focus` pseudo-class group (CSS Module 02 §2.4) for the same ten input types — changes `border-color` and adds a `box-shadow` focus ring, following the accessible focus-visibility guidance from CSS Module 07 §7.3 ("Focus state — CRITICAL: always visible"). Note: `outline: none` was set on the base rule (line 92) but is compensated for by this visible `box-shadow` ring — the correct pattern per Module 04 (HTML) / CSS Module 04's guidance that `outline: none` must always pair with a visible replacement.
  - **Line 111–123** — `select` element styling, matching the text-input visual language (border, radius, padding) plus `cursor: pointer` since a `<select>` is clickable.
  - **Line 125–128** — `select:focus` — matching focus ring, consistent with the input focus treatment.
  - **Line 130–144** — `textarea` styling, notably `resize: vertical` (CSS Module 07 §7.4 — "allow vertical resize only," preventing horizontal resize from breaking the form layout) and an explicit `font-family` re-declaration (since `<textarea>` doesn't reliably inherit `font-family` from `body` in all browsers, per CSS Module 07 §7.2's note that browsers don't do this by default).
  - **Line 146–149** — `textarea:focus` focus ring, consistent with other controls.
  - **Line 151–157** — `input[type="range"]` — full-width slider using `accent-color` (CSS Module 07 §7.6, "Approach 2: the simplest way to brand form controls") to tint the slider track/thumb without a hand-built custom slider.
  - **Line 159–168** — `input[type="color"]` — fixed small dimensions (60×36px) with padding and border to frame the native color swatch.
  - **Line 170–176** — `input[type="file"]` — minimal styling; file inputs are notoriously hard to restyle natively (courseware CSS Module 07 §7.1 notes this), so this file leaves the native picker button largely as-is.
  - **Line 178–186** — `input[type="radio"], input[type="checkbox"]` grouped — again using `accent-color` for brand-consistent native checkbox/radio rendering, with fixed 15×15px sizing and `vertical-align: middle` to align with adjacent label text.
  - **Line 188–191** — `output` — styles the `<output>` element (used on line 340 to live-echo the salary range value) in bold accent color.
  - **Line 193–207** — `input[type="submit"]` base + `:hover` — a solid brand-colored button with a darker `:hover` state (`#2c5f8a` → `#1e4468`), demonstrating a state-transition pattern (`transition: background-color 0.2s`).
  - **Line 209–224** — `input[type="reset"]` base + `:hover` — a neutral/secondary-styled button (light gray background) to visually de-emphasize the destructive "reset form" action relative to the primary submit button — a sound UX/CSS pattern even though not explicitly named in the courseware.
  - **Line 226–242** — `button` (the "Save as Draft" `<button type="button">`) + `:hover` — an outline/ghost-style button (transparent background, colored border+text, inverting to solid on hover) — visually a third tier of button (tertiary action).
  - **Line 244–247** — `br { display: block; margin: 2px 0; }` — an unusual rule that overrides the browser's default (inline, zero-height) rendering of `<br>` to instead behave as a block with a small margin. This is a non-standard technique to add breathing room between the many `<br>`-separated form rows used throughout the body (see below) — functionally works, but Module 05 (HTML) would instead recommend restructuring the layout with proper block-level wrapper `<div>`s/CSS spacing (as the CSS courseware's `.field` pattern in Module 07 §7.8 does) rather than co-opting `<br>` for layout spacing.

### Document body (lines 252–412)

- **Line 254** — Visible page `<h1>`, matching the WCAG one-`<h1>`-per-page rule from HTML Module 05 §5.1.
- **Line 256** — `<form action="/employees" method="post" enctype="multipart/form-data">` — `method="post"` is correct per HTML Module 10 §10.2 for a data-creation action; `enctype="multipart/form-data"` is **required** because the form contains `<input type="file">` elements (profile photo, resume) — exactly the rule stated in Module 10 §10.3/Key Takeaways.
- **Line 258–292 (Fieldset 1 — Personal Information)**:
  - **Line 259** — `<legend>Personal Information</legend>` labels the fieldset group, announced by screen readers before each contained input (Module 10 §10.7).
  - **Line 261–263** — Explicit `<label for="first_name">` / `<input id="first_name">` pairing (Module 10 §10.4's recommended pattern), plus `placeholder`, `autocomplete="given-name"` (a recognized autofill token), and the boolean `required` attribute.
  - **Line 266–267, 270–271** — Same explicit label/input/required pattern for last name and email; `type="email"` triggers built-in browser format validation (Module 10 §10.8) and `autocomplete="email"`.
  - **Line 274** — `type="tel"` phone input — no client-side pattern validation applied here (the courseware Module 10 §10.3 shows an optional `pattern="[0-9]{10}"` for this, which this file omits) and it is **not** marked `required`.
  - **Line 277** — `type="date"` — native date picker, no `min`/`max` constraints set (unlike the courseware's DOB example in Module 10 §10.3 which constrains `min="1900-01-01" max="2010-12-31"`).
  - **Line 279–287** — A **radio button group** for gender. Note: this group uses a plain `<label>Gender:</label>` (line 279) as a group heading rather than wrapping the four radios in their own nested `<fieldset><legend>`. Per HTML Module 10 §10.7, `<fieldset>`+`<legend>` is the **required** pattern for radio groups for full screen-reader group announcement — this file relies on the outer fieldset ("Personal Information") plus a plain label instead, which is a mild accessibility gap relative to the courseware's stated best practice. Each `<input type="radio" name="gender" value="...">` shares the same `name="gender"` so only one can be selected at a time; each is followed by its own `<label for="...">` matching its `id`.
  - **Line 290** — `<input type="file" accept="image/*">` — restricts the file picker to image MIME types via `accept`, matching HTML Module 08's guidance pattern for image uploads (though this specific `accept` syntax comes from Module 10 §10.3's file-input coverage).
- **Line 296–358 (Fieldset 2 — Job Details)**:
  - **Line 300** — `type="number"` with `min="1" step="1"` — numeric input constrained to positive integers (Module 10 §10.3).
  - **Line 306–314** — `<select id="department" name="department">` with a disabled-style empty first `<option value="">-- Select Department --</option>` placeholder pattern (Module 10 §10.6), followed by six real `<option>`s.
  - **Line 317–327** — A `<input list="job_titles_list">` paired with `<datalist id="job_titles_list">` — this is a native **autocomplete/combobox** pattern: the text input remains freely typeable, but the browser also offers the `<option>` values in `<datalist>` as suggestions. This specific combination is **not covered in the HTML courseware modules read** (Module 10 documents standard `<select>`/`<input>` types but not `<datalist>`) — it is valid, standard HTML5 the project uses beyond the courseware's explicit examples.
  - **Line 330–335** — `<select size="4">` — setting `size` greater than 1 turns the dropdown into a **scrollable list box** showing multiple options simultaneously rather than a collapsed dropdown (again a detail beyond the courseware's explicit `<select>` coverage, though `multiple`+`size` is mentioned in Module 10 §10.6 for genuinely multi-select lists — here it's used for single-select without `multiple`, purely to change the visual list style).
  - **Line 338** — `type="time"` — native time picker, no `min`/`max`/`step` constraints (courseware Module 10 §10.3 shows `step="900"` for 15-minute increments, unused here).
  - **Line 340–342** — `<output id="salary_output">500000</output>` embedded inside the `<label>` text, live-updated by an **inline `oninput` handler**: `oninput="document.getElementById('salary_output').value = this.value"`. This is a working live-value-display pattern for a `type="range"` slider (`min="300000" max="5000000" step="50000"`), but note the inline JS handler (`onclick`/`oninput` attributes) is an older, non-separation-of-concerns pattern — neither the HTML nor CSS courseware explicitly recommends inline event-handler attributes (JS courseware, not part of this guide's scope, would typically recommend `addEventListener` instead); it is nonetheless valid HTML and demonstrates the `<output>` element's intended purpose (displaying the result of a calculation/user action).
  - **Line 344–356** — A **checkbox group** for skills: six `<input type="checkbox" name="skills" value="...">` elements all sharing `name="skills"` — because checkboxes (unlike radios) allow multiple selections, the shared `name` causes the server to receive an array/multiple values for `skills`. As with the gender radios, this group is not wrapped in its own dedicated `<fieldset><legend>` — a repeat of the accessibility gap relative to Module 10 §10.7's stated requirement.
- **Line 362–389 (Fieldset 3 — Additional Information)**:
  - **Line 366** — `type="url"` — validates URL-shaped input format (Module 10 §10.3).
  - **Line 369, 372** — `type="month"` and `type="week"` pickers — both explicitly documented in HTML Module 10 §10.3's date/time input list.
  - **Line 375** — `type="color"` with a default `value="#0011fa"` (hex color) — native color-swatch picker (Module 10 §10.3).
  - **Line 378** — `type="search"` — semantically a search box (Module 10 §10.3), though not wired to any actual search action in this static demo.
  - **Line 381** — `type="file" accept=".pdf,.doc,.docx" multiple` — restricts to document file extensions and allows selecting multiple files at once (Module 10 §10.3's "Multiple files" example).
  - **Line 383** — `<input type="hidden" id="dept_id" name="dept_id" value="DEPT-0042">` — a hidden field carrying a fixed value to the server without any visible UI, exactly matching Module 10 §10.3's hidden-field pattern (used there for CSRF tokens/IDs).
  - **Line 386–387** — `<textarea rows="4" cols="50">` for free-text notes, matching Module 10 §10.5.
- **Line 392–402 (Fieldset 4 — Consent & Confirmation)**:
  - **Line 393** — `<legend>Consent &amp; Confirmation</legend>` — correctly uses the `&amp;` HTML entity to escape the literal ampersand character, per HTML Module 02 §2.8's entity-escaping rules.
  - **Line 395–397** — A `required` checkbox for data-processing consent — since this is a single standalone checkbox (not a mutually-exclusive group), it correctly does not need a `<fieldset>`/`<legend>` wrapper per Module 10 §10.3's single-checkbox example pattern; it does have its own `<label for="consent_data">`.
  - **Line 399–400** — A second, optional (non-`required`) checkbox for policy acknowledgment.
- **Line 406–408 (Form actions)**:
  - **Line 406** — `<input type="submit" value="Create Employee">` — submits the form (styled by the `input[type="submit"]` CSS rule above).
  - **Line 407** — `<input type="reset" value="Reset Form">` — clears all form fields back to their default values (styled by the secondary/neutral button CSS rule).
  - **Line 408** — `<button type="button" onclick="alert('Draft saved!')">Save as Draft</button>` — `type="button"` explicitly prevents this button from submitting the form (the default `<button>` type inside a `<form>` is `submit`, which would trigger an unwanted form submission) — a subtle but important HTML mechanic: **always set `type="button"` on non-submit buttons inside a `<form>`**.
- **Throughout the body** — the pervasive use of `<br><br>` after nearly every field is the primary layout mechanism for vertical spacing between form rows. This directly matches the anti-pattern HTML Module 05 §5.2 explicitly warns against ("❌ Using `<br>` to create paragraph spacing... ✅ Use CSS margin/padding instead") — though here it's applied to form-row spacing rather than paragraph spacing specifically, the same principle applies: the file relies on `<br>` (backed by the custom `br { display:block; margin:2px 0; }` CSS rule at line 244) rather than the more idiomatic Flexbox `.field` wrapper pattern shown in CSS Module 07 §7.8.

## C+D. `Code/Express/ems-ui/index.html` and `Code/Express/ems-ui/styles.css`

These two files are a matched pair (the HTML links directly to the CSS via `<link rel="stylesheet" href="styles.css">`), so they are explained together, HTML first then the CSS rules that target each part of it.

### C. `index.html`

```html
1:   <!DOCTYPE html>
2:   <html lang="en">
3:   
4:   <head>
5:     <meta charset="UTF-8">
6:     <meta name="viewport" content="width=device-width, initial-scale=1.0">
7:     <title>EMS — Employee Management System</title>
8:     <link rel="stylesheet" href="styles.css">
9:   </head>
10:  
11:  <body>
12:  
13:    <!-- ── Connection status bar (Socket.io signature) ─────── -->
14:    <header data-status="disconnected"></header>
15:  
16:    <!-- ── Login Form ──────────────────────────────────────── -->
17:    <form>
18:      <fieldset>
19:        <legend>EMS Portal</legend>
20:        <p>Sign in to your account</p>
21:  
22:        <label>
23:          Email
24:          <input id="login-email" type="email" placeholder="you@company.local" required autocomplete="username">
25:        </label>
26:  
27:        <label>
28:          Password
29:          <input id="login-password" type="password" placeholder="••••••••" required autocomplete="current-password">
30:        </label>
31:  
32:        <button type="submit">Sign in</button>
33:      </fieldset>
34:    </form>
35:  
36:    <!-- ── App Shell (hidden until authed) ─────────────────── -->
37:    <section style="display:none">
38:  
39:      <!-- Sidebar -->
40:      <nav>
41:        <span>EMS</span>
42:  
43:        <a data-page="dashboard">
44:          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
45:            <rect x="3" y="3" width="7" height="7" />
46:            <rect x="14" y="3" width="7" height="7" />
47:            <rect x="14" y="14" width="7" height="7" />
48:            <rect x="3" y="14" width="7" height="7" />
49:          </svg>
50:          <span>Dashboard</span>
51:        </a>
52:  
53:        <a data-page="employees">
54:          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
55:            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
56:            <circle cx="9" cy="7" r="4" />
57:            <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
58:            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
59:          </svg>
60:          <span>Employees</span>
61:        </a>
62:  
63:        <a data-page="departments">
64:          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
65:            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
66:            <polyline points="9 22 9 12 15 12 15 22" />
67:          </svg>
68:          <span>Departments</span>
69:        </a>
70:  
71:        <a data-page="projects">
72:          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
73:            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
74:          </svg>
75:          <span>Projects</span>
76:        </a>
77:  
78:        <a data-page="profile">
79:          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
80:            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
81:            <circle cx="12" cy="7" r="4" />
82:          </svg>
83:          <span>My Profile</span>
84:        </a>
85:  
86:        <footer>
87:          <strong>—</strong>
88:          <small>—</small>
89:          <button type="button" id="logout-btn" data-variant="ghost">Sign out</button>
90:        </footer>
91:      </nav>
92:  
93:      <!-- Main area -->
94:      <main>
95:        <!-- Topbar -->
96:        <div id="topbar">
97:          <h1 id="page-title">Dashboard</h1>
98:          <span id="status-dot" data-status="disconnected">
99:            <b></b>
100:           Offline
101:         </span>
102:       </div>
103: 
104:       <!-- Page content -->
105:       <article id="content">
106:         <p>Loading…</p>
107:       </article>
108:     </main>
109: 
110:   </section>
111: 
112:   <!-- ── Toast area ──────────────────────────────────────── -->
113:   <aside id="toasts"></aside>
114: 
115:   <!-- ── Modal ───────────────────────────────────────────── -->
116:   <dialog>
117:     <header>
118:       <h2>Dialog</h2>
119:       <button type="button" onclick="this.closest('dialog').close()">✕</button>
120:     </header>
121:     <form method="dialog">
122:       <fieldset></fieldset>
123:       <footer>
124:         <button type="button" data-variant="ghost" id="modal-cancel">Cancel</button>
125:         <button type="submit" id="modal-submit">Save</button>
126:       </footer>
127:     </form>
128:   </dialog>
129: 
130:   <!-- Socket.io client (from your running server) -->
131:   <script src="http://localhost:3000/socket.io/socket.io.js"></script>
132:   <script src="script.js"></script>
133: </body>
134: 
135: </html>
```

- **Line 1–2** — Standard `<!DOCTYPE html>` + `<html lang="en">`, both correct per Module 04.
- **Line 5–7** — `<meta charset="UTF-8">` first, then viewport, then `<title>` — matches the exact ordering recommended in Module 04's boilerplate (§4.1).
- **Line 8** — `<link rel="stylesheet" href="styles.css">` — an **external stylesheet** (CSS Module 01 §1.3's recommended, cacheable approach), correctly placed in `<head>` so it loads before body content renders (avoiding FOUC per Module 04 §4.4.5).
- **Line 14** — `<header data-status="disconnected"></header>` — an **empty `<header>` element used purely as a status indicator**, not as a semantic page-header-with-content. Its only content is a `data-*` custom attribute (HTML Module 02 §2.4/Module 12 §12.3) whose value (`disconnected`/`connecting`/`connected`, presumably toggled by `script.js` at runtime) drives CSS styling — see styles.css lines 41–56, where `header[data-status="..."]` attribute selectors (CSS Module 02 §2.3) change its background color. This is a clever repurposing of `data-*` as a CSS state hook rather than JS-only data storage — a "Socket.io connection status bar" as the HTML comment (line 13) explains.
- **Line 17–34** — The login `<form>` (no `action`/`method` attributes — this app is a JS-driven SPA that presumably intercepts `submit` via `script.js` rather than doing a traditional HTML form POST).
  - **Line 18–19** — `<fieldset><legend>EMS Portal</legend>` groups the whole login form, with the legend acting as the form's title.
  - **Line 22–25, 27–30** — **Implicit labels** (HTML Module 10 §10.4's "Implicit Label" pattern) — the `<input>` is nested directly *inside* the `<label>` rather than using a separate `for`/`id` pairing, which is valid but less commonly emphasized than the explicit pattern in the courseware. `type="email"` (line 24) and `type="password"` (line 29) are both `required` with sensible `autocomplete` tokens (`username`, `current-password`) matching browser password-manager conventions.
  - **Line 32** — `<button type="submit">Sign in</button>` — explicit `type="submit"`, correctly submitting the enclosing form.
- **Line 37** — `<section style="display:none">` — the entire authenticated app shell is hidden via an **inline style** until login succeeds (presumably toggled by JS). This is a legitimate use of inline styles for a dynamic, JS-controlled state per CSS Module 01 §1.3's carve-out ("useful for dynamic styles generated by JavaScript"), rather than a static presentational choice.
- **Line 40–91 (Sidebar `<nav>`)**:
  - **Line 41** — `<span>EMS</span>` — the app logo/wordmark, styled via `nav > span` in the CSS (a **child combinator**, CSS Module 02 §2.6).
  - **Line 43–84** — Four navigation links, each `<a data-page="...">` (note: **no `href` attribute** — since navigation is handled entirely client-side by `script.js` reading `data-page`, not by real page loads; this differs from the courseware's standard `<a href="/dashboard">` pattern in HTML Module 06, reflecting this file's SPA architecture). Each link contains an **inline SVG icon** (HTML Module 08 §8.6's "Inline SVG: can be styled with CSS" pattern) followed by a `<span>` text label. The SVGs use `fill="none" stroke="currentColor"` so their color inherits the link's CSS `color` (styled via `nav > a > svg` in the CSS, and `currentColor` dynamically tracks whatever color is set on the parent `<a>`).
  - **Line 86–90** — `<footer>` nested inside `<nav>` — an HTML5 semantic-content pattern (footer scoped to its nearest sectioning ancestor, here the sidebar `<nav>`, matching Module 11 §11.2's principle that `<footer>` provides closing content "for a page **or section**"). Contains placeholder `<strong>—</strong>`/`<small>—</small>` (populated with user name/role by JS at runtime) and a `<button data-variant="ghost">` sign-out control, where `data-variant` is a custom attribute used purely as a CSS styling hook (see styles.css `button[data-variant="ghost"]`).
- **Line 94–108 (Main content area)**:
  - **Line 96–102** — `<div id="topbar">` containing an `<h1 id="page-title">Dashboard</h1>` (dynamically retitled by JS as the user navigates) and a connection-status `<span id="status-dot" data-status="disconnected">` with a nested empty `<b></b>` (styled as a colored dot circle via CSS — `main > div > span > b { border-radius: 50%; }`) plus literal text "Offline". This reuses the same `data-status` pattern as the top-level `<header>` on line 14 for a second, smaller status indicator.
  - **Line 105–107** — `<article id="content"><p>Loading…</p></article>` — the main content pane, semantically an `<article>` (self-contained unit, HTML Module 11 §11.3) whose inner HTML is entirely replaced at runtime by `script.js` depending on which nav link (`data-page`) is active — the "Loading…" text is a placeholder shown before the first render.
- **Line 113** — `<aside id="toasts"></aside>` — an empty container semantically marked `<aside>` (tangential/supplementary content, Module 11 §11.3) that JS populates with transient toast notifications (`<output>` elements, per styles.css line 481).
- **Line 116–128 (Modal `<dialog>`)**:
  - **Line 116** — `<dialog>` — the native HTML5 modal element (HTML Module 11 §11.5's "Native modal dialogs (no JavaScript library needed)"), controlled via the `.showModal()`/`.close()` JS API (invoked by `script.js`, not shown in this file).
  - **Line 119** — `<button type="button" onclick="this.closest('dialog').close()">✕</button>` — an inline-handler close button using `this.closest('dialog')` to find its ancestor `<dialog>` and call `.close()` — a DOM traversal pattern, using an inline event handler (as also seen in file B).
  - **Line 121** — `<form method="dialog">` — a special form `method` value that, on submit, automatically closes the parent `<dialog>` without a network request — this is native HTML5 `<dialog>`+`<form>` integration, not explicitly covered in the HTML courseware modules read (which cover `<dialog>` conceptually in Module 11 §11.5 but don't document `method="dialog"` specifically) — valid modern HTML beyond the courseware's example.
  - **Line 122** — `<fieldset></fieldset>` — an empty fieldset, presumably populated dynamically by JS with different form fields depending on which modal action triggered it (add employee, edit department, etc. — a reusable modal shell).
  - **Line 124–125** — Footer buttons: `data-variant="ghost"` Cancel button (`type="button"`, does not submit) and a `type="submit"` Save button (submits the `method="dialog"` form, closing the dialog natively).
- **Line 131** — `<script src="http://localhost:3000/socket.io/socket.io.js">` — loads the Socket.io client library from the app's own Node/Express backend (real-time WebSocket connectivity, matching the "connection status bar" concept from lines 13–14).
- **Line 132** — `<script src="script.js">` — the app's own logic (not part of this HTML/CSS-focused guide). Both scripts are placed at the **end of `<body>`**, matching the traditional "scripts at end of body" loading strategy from HTML Module 04 §4.5 (ensures the DOM is fully parsed before scripts run, without needing `defer`).

### D. `styles.css`

```css
1:   /* ── Reset & Base ─────────────────────────────────────────── */
2:   *, *::before, *::after {
3:     box-sizing: border-box;
4:     margin: 0;
5:     padding: 0;
6:   }
7:   
8:    :root {
9:     --bg:        #0f172a;
10:    --surface:   #1e293b;
11:    --border:    #334155;
12:    --text:      #f1f5f9;
13:    --muted:     #94a3b8;
14:    --accent:    #3b82f6;
15:    --accent-bg: #1e3a8a;
16:    --danger:    #ef4444;
17:    --success:   #22c55e;
18:    --warn:      #f59e0b;
19:    --sidebar-w: 200px;
20:    --topbar-h:  52px;
21:    --radius:    6px;
22:    --mono:      'JetBrains Mono', 'Fira Mono', 'Consolas', monospace;
23:  }
24:  
25:  
26:  html {
27:    font-size: 14px;
28:    font-family: system-ui, -apple-system, sans-serif;
29:    color: var(--text);
30:    background: var(--bg);
31:  }
32:  
33:  body {
34:    display: flex;
35:    flex-direction: column;
36:    height: 100vh;
37:    overflow: hidden;
38:  }
39:  
40:  /* ── Connection Status Bar (Socket.io signature element) ──── */
41:  header {
42:    height: 3px;
43:    background: var(--border);
44:    position: relative;
45:    flex-shrink: 0;
46:    transition: background 0.4s;
47:  }
48:  
49:  header[data-status="connected"]    { background: var(--success); }
50:  header[data-status="disconnected"] { background: var(--danger); }
51:  header[data-status="connecting"]   { background: var(--warn); animation: pulse 1.2s infinite; }
52:  
53:  @keyframes pulse {
54:    0%, 100% { opacity: 1; }
55:    50%       { opacity: 0.4; }
56:  }
57:  
58:  /* ── App Shell ───────────────────────────────────────────── */
59:  section {          /* wraps sidebar + main */
60:    display: flex;
61:    flex: 1;
62:    overflow: hidden;
63:  }
64:  
65:  /* ── Sidebar ─────────────────────────────────────────────── */
66:  nav {
67:    width: var(--sidebar-w);
68:    background: var(--surface);
69:    border-right: 1px solid var(--border);
70:    display: flex;
71:    flex-direction: column;
72:    padding: 0;
73:    flex-shrink: 0;
74:    overflow-y: auto;
75:  }
76:  
77:  nav > span {          /* logo / app name */
78:    display: block;
79:    padding: 16px 20px 14px;
80:    font-size: 13px;
81:    font-weight: 700;
82:    letter-spacing: 0.08em;
83:    text-transform: uppercase;
84:    color: var(--accent);
85:    border-bottom: 1px solid var(--border);
86:  }
87:  
88:  nav > a {
89:    display: flex;
90:    align-items: center;
91:    gap: 10px;
92:    padding: 11px 20px;
93:    color: var(--muted);
94:    text-decoration: none;
95:    font-size: 13px;
96:    font-weight: 500;
97:    border-left: 3px solid transparent;
98:    transition: color 0.15s, background 0.15s;
99:    cursor: pointer;
100: }
101: 
102: nav > a:hover {
103:   color: var(--text);
104:   background: var(--bg);
105: }
106: 
107: nav > a[data-active] {
108:   color: var(--accent);
109:   background: var(--accent-bg);
110:   border-left-color: var(--accent);
111: }
112: 
113: nav > a > svg {
114:   width: 15px;
115:   height: 15px;
116:   flex-shrink: 0;
117:   opacity: 0.8;
118: }
119: 
120: nav > footer {       /* bottom of sidebar: user info */
121:   margin-top: auto;
122:   padding: 14px 20px;
123:   border-top: 1px solid var(--border);
124:   font-size: 12px;
125:   color: var(--muted);
126: }
127: 
128: nav > footer > strong {
129:   display: block;
130:   color: var(--text);
131:   margin-bottom: 2px;
132: }
133: 
134: nav > footer > button {
135:   margin-top: 8px;
136:   width: 100%;
137: }
138: 
139: /* ── Main Area ───────────────────────────────────────────── */
140: main {
141:   flex: 1;
142:   display: flex;
143:   flex-direction: column;
144:   overflow: hidden;
145: }
146: 
147: /* topbar */
148: main > div {     /* #topbar */
149:   height: var(--topbar-h);
150:   border-bottom: 1px solid var(--border);
151:   background: var(--surface);
152:   display: flex;
153:   align-items: center;
154:   padding: 0 24px;
155:   gap: 12px;
156:   flex-shrink: 0;
157: }
158: 
159: main > div > h1 {
160:   font-size: 15px;
161:   font-weight: 600;
162:   flex: 1;
163: }
164: 
165: main > div > span {   /* socket status label */
166:   font-size: 11px;
167:   color: var(--muted);
168:   display: flex;
169:   align-items: center;
170:   gap: 5px;
171: }
172: 
173: main > div > span > b {  /* coloured dot */
174:   display: inline-block;
175:   width: 7px;
176:   height: 7px;
177:   border-radius: 50%;
178:   background: var(--border);
179: }
180: 
181: main > div > span[data-status="connected"]    > b { background: var(--success); }
182: main > div > span[data-status="disconnected"] > b { background: var(--danger); }
183: main > div > span[data-status="connecting"]   > b { background: var(--warn); }
184: 
185: /* content pane */
186: main > article {
187:   flex: 1;
188:   overflow-y: auto;
189:   padding: 24px;
190: }
191: 
192: /* ── Login Page ──────────────────────────────────────────── */
193: body > form {        /* login form, shown when not authed */
194:   position: fixed;
195:   inset: 0;
196:   display: flex;
197:   align-items: center;
198:   justify-content: center;
199:   background: var(--bg);
200:   z-index: 100;
201: }
202: 
203: body > form > fieldset {
204:   background: var(--surface);
205:   border: 1px solid var(--border);
206:   border-radius: var(--radius);
207:   padding: 36px 40px;
208:   width: 360px;
209:   display: flex;
210:   flex-direction: column;
211:   gap: 16px;
212: }
213: 
214: body > form > fieldset > legend {
215:   font-size: 18px;
216:   font-weight: 700;
217:   padding: 0 0 4px;
218:   margin-bottom: 4px;
219:   color: var(--text);
220:   border: none;
221: }
222: 
223: body > form > fieldset > p {    /* subtitle */
224:   font-size: 12px;
225:   color: var(--muted);
226:   margin-top: -8px;
227: }
228: 
229: /* ── Forms (generic) ─────────────────────────────────────── */
230: label {
231:   display: flex;
232:   flex-direction: column;
233:   gap: 5px;
234:   font-size: 12px;
235:   font-weight: 600;
236:   color: var(--muted);
237:   text-transform: uppercase;
238:   letter-spacing: 0.05em;
239: }
240: 
241: input, select, textarea {
242:   width: 100%;
243:   padding: 8px 10px;
244:   font-size: 13px;
245:   font-family: inherit;
246:   border: 1px solid var(--border);
247:   border-radius: var(--radius);
248:   background: var(--surface);
249:   color: var(--text);
250:   outline: none;
251:   transition: border-color 0.15s;
252: }
253: 
254: input:focus, select:focus, textarea:focus {
255:   border-color: var(--accent);
256: }
257: 
258: textarea {
259:   resize: vertical;
260:   min-height: 80px;
261: }
262: 
263: /* ── Buttons ─────────────────────────────────────────────── */
264: button {
265:   padding: 8px 16px;
266:   font-size: 13px;
267:   font-family: inherit;
268:   font-weight: 600;
269:   border: 1px solid var(--accent);
270:   border-radius: var(--radius);
271:   background: var(--accent);
272:   color: #fff;
273:   cursor: pointer;
274:   transition: opacity 0.15s;
275:   white-space: nowrap;
276: }
277: 
278: button:hover   { opacity: 0.85; }
279: button:active  { opacity: 0.7; }
280: button:disabled { opacity: 0.45; cursor: not-allowed; }
281: 
282: button[data-variant="ghost"] {
283:   background: transparent;
284:   color: var(--muted);
285:   border-color: var(--border);
286: }
287: 
288: button[data-variant="ghost"]:hover {
289:   color: var(--text);
290:   border-color: var(--muted);
291: }
292: 
293: button[data-variant="danger"] {
294:   background: var(--danger);
295:   border-color: var(--danger);
296:   color: #fff;
297: }
298: 
299: button[data-variant="sm"] {
300:   padding: 5px 10px;
301:   font-size: 12px;
302: }
303: 
304: /* ── Tables ──────────────────────────────────────────────── */
305: table {
306:   width: 100%;
307:   border-collapse: collapse;
308:   font-size: 13px;
309:   background: var(--surface);
310:   border: 1px solid var(--border);
311:   border-radius: var(--radius);
312:   overflow: hidden;
313: }
314: 
315: thead {
316:   background: var(--bg);
317: }
318: 
319: th {
320:   text-align: left;
321:   padding: 10px 14px;
322:   font-size: 11px;
323:   font-weight: 700;
324:   text-transform: uppercase;
325:   letter-spacing: 0.06em;
326:   color: var(--muted);
327:   border-bottom: 1px solid var(--border);
328:   white-space: nowrap;
329: }
330: 
331: td {
332:   padding: 10px 14px;
333:   border-bottom: 1px solid var(--border);
334:   vertical-align: middle;
335:   color: var(--text);
336: }
337: 
338: tr:last-child > td { border-bottom: none; }
339: 
340: tbody > tr:hover { background: var(--bg); }
341: 
342: /* ── Badges (via data attrs on td/span) ─────────────────── */
343: td > em, span[data-badge] {
344:   display: inline-block;
345:   padding: 2px 8px;
346:   border-radius: 20px;
347:   font-size: 11px;
348:   font-weight: 600;
349:   font-style: normal;
350:   background: var(--border);
351:   color: var(--muted);
352:   text-transform: capitalize;
353: }
354: 
355: span[data-badge="active"],
356: span[data-badge="completed"] { background: #dcfce7; color: #15803d; }
357: 
358: span[data-badge="inactive"],
359: span[data-badge="on-hold"]   { background: #fee2e2; color: #b91c1c; }
360: 
361: span[data-badge="planning"]  { background: #fef9c3; color: #a16207; }
362: 
363: span[data-badge="admin"]     { background: #ede9fe; color: #6d28d9; }
364: span[data-badge="manager"]   { background: #dbeafe; color: #1d4ed8; }
365: span[data-badge="employee"]  { background: var(--bg); color: var(--muted); }
366: 
367: /* ── Toolbar (search + actions row above table) ──────────── */
368: nav + main > article > div {   /* toolbar */
369:   display: flex;
370:   align-items: center;
371:   gap: 10px;
372:   margin-bottom: 16px;
373:   flex-wrap: wrap;
374: }
375: 
376: nav + main > article > div > input {
377:   width: 220px;
378:   flex-shrink: 0;
379: }
380: 
381: nav + main > article > div > select {
382:   width: auto;
383: }
384: 
385: /* ── Pagination ──────────────────────────────────────────── */
386: nav + main > article > footer {
387:   display: flex;
388:   align-items: center;
389:   justify-content: space-between;
390:   margin-top: 14px;
391:   font-size: 12px;
392:   color: var(--muted);
393: }
394: 
395: nav + main > article > footer > div {
396:   display: flex;
397:   gap: 6px;
398: }
399: 
400: /* ── Modal ───────────────────────────────────────────────── */
401: dialog {
402:   border: 1px solid var(--border);
403:   border-radius: var(--radius);
404:   padding: 0;
405:   width: 480px;
406:   max-width: 95vw;
407:   background: var(--surface);
408:   box-shadow: 0 20px 60px rgba(0,0,0,0.12);
409: }
410: 
411: dialog::backdrop {
412:   background: rgba(0,0,0,0.35);
413: }
414: 
415: dialog > header {
416:   display: flex;
417:   align-items: center;
418:   justify-content: space-between;
419:   padding: 18px 24px;
420:   border-bottom: 1px solid var(--border);
421:   height: auto;
422:   background: none;
423:   animation: none;
424: }
425: 
426: dialog > header > h2 {
427:   font-size: 15px;
428:   font-weight: 700;
429: }
430: 
431: dialog > header > button {
432:   background: none;
433:   border: none;
434:   color: var(--muted);
435:   font-size: 18px;
436:   padding: 0 4px;
437:   line-height: 1;
438: }
439: 
440: dialog > form {
441:   padding: 20px 24px;
442:   display: flex;
443:   flex-direction: column;
444:   gap: 14px;
445:   position: static;
446:   background: none;
447:   border: none;
448:   width: auto;
449: }
450: 
451: dialog > form > fieldset {
452:   border: none;
453:   padding: 0;
454:   display: flex;
455:   flex-direction: column;
456:   gap: 14px;
457: }
458: 
459: dialog > form > footer {
460:   display: flex;
461:   justify-content: flex-end;
462:   gap: 8px;
463:   padding: 0;
464:   border: none;
465:   background: none;
466:   margin-top: 4px;
467: }
468: 
469: /* ── Toast Notifications ─────────────────────────────────── */
470: aside {
471:   position: fixed;
472:   bottom: 20px;
473:   right: 20px;
474:   display: flex;
475:   flex-direction: column;
476:   gap: 8px;
477:   z-index: 999;
478:   pointer-events: none;
479: }
480: 
481: aside > output {
482:   display: block;
483:   padding: 10px 16px;
484:   background: var(--text);
485:   color: #fff;
486:   border-radius: var(--radius);
487:   font-size: 13px;
488:   max-width: 300px;
489:   pointer-events: auto;
490:   animation: slide-in 0.2s ease;
491:   box-shadow: 0 4px 12px rgba(0,0,0,0.15);
492: }
493: 
494: aside > output[data-type="error"]   { background: var(--danger); }
495: aside > output[data-type="success"] { background: var(--success); }
496: aside > output[data-type="warn"]    { background: var(--warn); }
497: 
498: @keyframes slide-in {
499:   from { transform: translateX(20px); opacity: 0; }
500:   to   { transform: translateX(0);    opacity: 1; }
501: }
502: 
503: /* ── Stats Cards (Dashboard) ─────────────────────────────── */
504: main > article > ul {
505:   display: grid;
506:   grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
507:   gap: 14px;
508:   list-style: none;
509:   margin-bottom: 24px;
510: }
511: 
512: main > article > ul > li {
513:   background: var(--surface);
514:   border: 1px solid var(--border);
515:   border-radius: var(--radius);
516:   padding: 18px 20px;
517: }
518: 
519: main > article > ul > li > span {
520:   display: block;
521:   font-size: 11px;
522:   font-weight: 700;
523:   text-transform: uppercase;
524:   letter-spacing: 0.06em;
525:   color: var(--muted);
526:   margin-bottom: 6px;
527: }
528: 
529: main > article > ul > li > strong {
530:   display: block;
531:   font-size: 28px;
532:   font-weight: 800;
533:   color: var(--accent);
534:   font-family: var(--mono);
535: }
536: 
537: main > article > ul > li > small {
538:   display: block;
539:   font-size: 11px;
540:   color: var(--muted);
541:   margin-top: 4px;
542: }
543: 
544: /* ── Section heading ─────────────────────────────────────── */
545: main > article > h2 {
546:   font-size: 13px;
547:   font-weight: 700;
548:   text-transform: uppercase;
549:   letter-spacing: 0.07em;
550:   color: var(--muted);
551:   margin-bottom: 12px;
552:   margin-top: 24px;
553: }
554: 
555: main > article > h2:first-child { margin-top: 0; }
556: 
557: /* ── Empty state ─────────────────────────────────────────── */
558: main > article > p {
559:   color: var(--muted);
560:   font-size: 13px;
561:   padding: 40px 0;
562:   text-align: center;
563: }
564: 
565: /* ── Detail key-value grid ───────────────────────────────── */
566: main > article > dl {
567:   display: grid;
568:   grid-template-columns: 160px 1fr;
569:   gap: 0;
570:   background: var(--surface);
571:   border: 1px solid var(--border);
572:   border-radius: var(--radius);
573:   overflow: hidden;
574:   font-size: 13px;
575:   margin-bottom: 16px;
576: }
577: 
578: main > article > dl > dt {
579:   padding: 10px 16px;
580:   background: var(--bg);
581:   font-weight: 600;
582:   font-size: 11px;
583:   text-transform: uppercase;
584:   letter-spacing: 0.05em;
585:   color: var(--muted);
586:   border-bottom: 1px solid var(--border);
587: }
588: 
589: main > article > dl > dd {
590:   padding: 10px 16px;
591:   border-bottom: 1px solid var(--border);
592:   color: var(--text);
593: }
594: 
595: main > article > dl > dt:last-of-type,
596: main > article > dl > dd:last-of-type {
597:   border-bottom: none;
598: }
599: 
600: /* ── Scrollbars ──────────────────────────────────────────── */
601: ::-webkit-scrollbar       { width: 6px; height: 6px; }
602: ::-webkit-scrollbar-track { background: transparent; }
603: ::-webkit-scrollbar-thumb { background: var(--border); border-radius: 3px; }
604: 
605: /* ── Responsive ──────────────────────────────────────────── */
606: @media (max-width: 640px) {
607:   nav { width: 48px; }
608:   nav > span, nav > a > span, nav > footer > strong, nav > footer > small { display: none; }
609:   nav > a { justify-content: center; padding: 12px; }
610:   dialog { width: 95vw; }
611: }
612: 
613: 
614: /* Dark mode enhancements */
615: html,
616: body {
617:     background: var(--bg);
618:     color: var(--text);
619: }
620: 
621: thead { background: #172033; }
622: 
623: tbody > tr:hover { background: #243244; }
624: 
625: input,
626: select,
627: textarea {
628:     background: #273549;
629:     border-color: var(--border);
630:     color: var(--text);
631: }
632: 
633: input::placeholder,
634: textarea::placeholder { color: var(--muted); }
635: 
636: button[data-variant="ghost"] {
637:     background: transparent;
638:     color: var(--muted);
639: }
640: 
641: button[data-variant="ghost"]:hover {
642:     background: #273549;
643:     color: var(--text);
644: }
645: 
646: dialog { box-shadow: 0 24px 80px rgba(0,0,0,.6); }
647: 
648: ::-webkit-scrollbar-thumb { background: #475569; }
649: 
650: aside > output { background: #111827; }
```

- **Line 2–6** — `*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }` — the **universal reset** taught in CSS Module 04 §4.1 ("Always Use `border-box`... the universal reset") combined with a full margin/padding zero-out (the "Modern CSS Reset" style from CSS Module 08 §8.1). This is the industry-standard first rule in a professional stylesheet.
- **Line 8–23** — `:root { --bg: ...; --surface: ...; ... }` — a full **design-token custom-property system** exactly matching CSS Module 03 §3.5 and Module 08 §8.3's guidance: a dark-themed color palette (`--bg`, `--surface`, `--border`, `--text`, `--muted`, `--accent`, `--accent-bg`, `--danger`, `--success`, `--warn`), layout constants (`--sidebar-w: 200px`, `--topbar-h: 52px`), a shared `--radius: 6px`, and a monospace font stack `--mono` for numeric/stat displays. Every subsequent rule in the file consumes these via `var(--name)` rather than hardcoding colors — this is the "semantic design tokens" pattern from Module 08 §8.3, applied consistently across the whole file (though here tokens are defined directly as semantic names rather than layered primitive→semantic, a simplified single-tier version of that pattern).
- **Line 26–31** — `html { font-size: 14px; font-family: system-ui...; color: var(--text); background: var(--bg); }` — sets the **root font-size** (14px, smaller than the courseware's typical 16px baseline — a deliberate compact "admin dashboard" density choice) which all `rem` values elsewhere would reference (though this file uses `px` throughout rather than `rem`, so the root font-size mainly matters for unitless `em`/inherited text sizing); uses the **system font stack** pattern from CSS Module 06 §6.1; sets `color`/`background` here so they **inherit** down to all text (CSS Module 03 §3.2 — `color` is an inherited property).
- **Line 33–38** — `body { display: flex; flex-direction: column; height: 100vh; overflow: hidden; }` — turns `<body>` itself into a **flex container** stacking its direct children (the status-bar `<header>`, the login `<form>`, the app `<section>`, etc.) vertically, locked to exactly the viewport height (`100vh`, CSS Module 03 §3.4's viewport unit) with `overflow: hidden` to prevent the whole page from scrolling — instead, only specific inner panes (like `main > article`, line 186–190) scroll independently. This is a classic **full-height app-shell layout** built with Flexbox (CSS Module 05 §5.3).
- **Line 41–47** — `header { height: 3px; background: var(--border); position: relative; flex-shrink: 0; transition: background 0.4s; }` — styles the connection-status bar as a thin 3px strip. `flex-shrink: 0` (CSS Module 05 §5.3, flex item property) prevents this bar from being squeezed by its flex-container parent (`body`) when space is tight — it should always stay exactly 3px tall. `transition: background 0.4s` animates color changes smoothly.
- **Line 49–51** — `header[data-status="connected"]`, `header[data-status="disconnected"]`, `header[data-status="connecting"]` — **attribute selectors** (CSS Module 02 §2.3) matching the `data-status` value set on the `<header>` element (HTML line 14). This is the CSS side of the `data-*`-as-styling-hook pattern: JS toggles the attribute value at runtime, and these rules react automatically — no class manipulation needed. The `connecting` state adds `animation: pulse 1.2s infinite`.
- **Line 53–56** — `@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }` — a simple opacity pulse animation, not explicitly covered in the CSS courseware modules read (animations/`@keyframes` are outside the 9 modules' scope), but valid standard CSS used here for the "connecting" transient state.
- **Line 59–63** — `section { display: flex; flex: 1; overflow: hidden; }` — the `<section>` wrapping sidebar+main (HTML line 37) is itself a **flex container** (Flexbox again, nested inside the `body` flex layout) that takes up all remaining vertical space (`flex: 1`, CSS Module 05 §5.3's flex-grow shorthand) and arranges its two children (`<nav>` sidebar and `<main>`) **side by side** (default `flex-direction: row`).
- **Line 66–75** — `nav { width: var(--sidebar-w); ...; display: flex; flex-direction: column; ...; flex-shrink: 0; overflow-y: auto; }` — the sidebar is a fixed-width (200px via the token) flex container, itself using **column-direction Flexbox** to stack the logo, nav links, and footer vertically. `flex-shrink: 0` prevents the sidebar from being compressed by its parent's flex layout. `overflow-y: auto` allows the nav-link list to scroll independently if it overflows.
- **Line 77–86** — `nav > span` (a **child combinator**, CSS Module 02 §2.6, targeting only the `<span>EMS</span>` logo, not any other descendant span) — styled as an uppercase, letter-spaced, accent-colored wordmark with a bottom border separating it from the nav links below.
- **Line 88–100** — `nav > a` — styles all four sidebar nav links as flex rows (`display:flex; align-items:center; gap:10px`) aligning their icon and text, with a `border-left: 3px solid transparent` — a common technique of **reserving space for a border that appears on hover/active state** (avoids layout shift when the border becomes visible, since transparent still occupies the 3px).
- **Line 102–105** — `nav > a:hover` — hover state lightens text color and adds a subtle background tint.
- **Line 107–111** — `nav > a[data-active]` — an **attribute selector matching the mere presence of `data-active`** (any value, or none at all — Module 02 §2.3's "has the attribute" pattern `[required]`), applied by JS to whichever nav link corresponds to the current page. This fills in the border-left (previously transparent) with the accent color and swaps text/background to the accent palette — the active-page indicator.
- **Line 113–118** — `nav > a > svg` — a further-nested child combinator sizing the inline SVG icons to a consistent 15×15px with slightly reduced opacity for visual restraint.
- **Line 120–137** — `nav > footer` and its children (`strong`, `button`) — the sidebar's bottom-anchored user-info block. `margin-top: auto` (line 121) is the classic Flexbox "push to the end" trick (CSS Module 05 §5.3's pattern, e.g. `.nav .nav-logo { margin-right: auto; }` but applied vertically here since `nav` is a column-direction flex container) — this single declaration shoves the `<footer>` all the way to the bottom of the sidebar regardless of how many nav links precede it.
- **Line 140–145** — `main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }` — the main content region takes remaining horizontal space from the `section` flex row, and is itself a column flex container for its own topbar+article children.
- **Line 148–157** — `main > div` (the `#topbar`) — a fixed-height (`var(--topbar-h)`, 52px) horizontal flex row (`display:flex; align-items:center`) with `flex-shrink:0` to stay a constant height even as content changes.
- **Line 159–163** — `main > div > h1 { flex: 1; }` — the page-title heading takes up all available horizontal space in the topbar flex row, pushing the status indicator (next sibling) to the far right — another Flexbox space-distribution pattern.
- **Line 165–183** — `main > div > span` (the status label) and its nested `> b` (the colored dot) — mirrors the same `data-status` attribute-selector pattern used on the top-level `<header>` (lines 49–51), but here applied to `main > div > span[data-status="..."] > b`, coloring only the small circular dot (`border-radius: 50%`, CSS Module 04 §4.5's circle technique) rather than a whole bar.
- **Line 186–190** — `main > article { flex: 1; overflow-y: auto; padding: 24px; }` — the actual content pane scrolls independently (`overflow-y: auto`) while everything else (sidebar, topbar) stays fixed — this is what makes the app-shell layout feel like a native desktop app rather than a normal scrolling webpage.
- **Line 193–201** — `body > form { position: fixed; inset: 0; display: flex; align-items: center; justify-content: center; background: var(--bg); z-index: 100; }` — the login form is positioned `fixed` and covers the entire viewport (`inset: 0` — CSS Module 05 §5.5's shorthand for `top/right/bottom/left: 0`), then uses Flexbox centering (`align-items:center; justify-content:center` — CSS Module 05 §5.3's "Vertical centring" pattern) to center its child `<fieldset>` both horizontally and vertically. `z-index: 100` (against the token scale conceptually mirroring Module 05 §5.5's z-index guidance) ensures it sits above the (hidden) app shell during login. Note: once the user is authenticated, JS presumably sets `display:none` on this form and removes `display:none` from the app `<section>` — the two states are mutually exclusive full-screen overlays.
- **Line 203–212** — `body > form > fieldset` — the visible login card: fixed 360px width, dark surface background, rounded corners, padding, and itself a **column-direction flex container** (`display:flex; flex-direction:column; gap:16px`) neatly spacing its own children (legend, subtitle, two labeled inputs, submit button) without needing manual margins on each — the `gap` property (CSS Module 05 §5.3) is doing all the spacing work here.
- **Line 214–221** — `body > form > fieldset > legend` — overrides the browser's default `<legend>` rendering (which normally sits half-overlapping the fieldset's border) to instead read as a plain large bold title (`border: none` removes any default legend border quirks).
- **Line 223–227** — `body > form > fieldset > p` — the "Sign in to your account" subtitle, pulled up closer to the legend via a **negative margin** (`margin-top: -8px`) — a common fine-tuning technique for tightening vertical rhythm.
- **Line 230–239** — `label { display: flex; flex-direction: column; gap: 5px; ... }` — this is the key line that makes the **implicit label pattern** (input nested inside `<label>` per HTML Module 10 §10.4) work visually: turning each `<label>` into its own small column-flex container so the label text sits above its nested `<input>` with a small `5px` gap, rather than inline. Also styled as a small, uppercase, letter-spaced "form field caption" — a common enterprise-dashboard label treatment.
- **Line 241–256** — `input, select, textarea` — a **grouped type selector** (CSS Module 02 §2.7) giving every form control a consistent base appearance (full width, padding, border, radius, dark surface background) — note `font-family: inherit` (line 245) is present, exactly matching CSS Module 07 §7.2's explicit instruction ("browsers don't do this by default"). The shared `:focus` rule (line 254–256) simply swaps `border-color` to the accent color — a lighter-weight focus treatment than file B's box-shadow ring, but still satisfies the "always visible focus" requirement from CSS Module 07 §7.3.
- **Line 258–261** — `textarea { resize: vertical; min-height: 80px; }` — matches CSS Module 07 §7.4's exact guidance (`resize: vertical` prevents horizontal-resize layout breakage).
- **Line 264–276** — `button` base styles — solid accent-colored button (background **and** border both `var(--accent)`), white text, rounded, with `white-space: nowrap` (prevents button label text wrapping awkwardly) and an opacity-based hover/active transition rather than a color-swap transition.
- **Line 278–280** — `button:hover { opacity: 0.85; }`, `button:active { opacity: 0.7; }`, `button:disabled { opacity: 0.45; cursor: not-allowed; }` — a **single-property opacity state ladder** for interaction feedback — simpler than defining new background colors for each state, and automatically works for any button color variant since opacity is multiplicative over whatever color is already applied.
- **Line 282–302** — `button[data-variant="ghost"]`, `button[data-variant="danger"]`, `button[data-variant="sm"]` — **attribute selectors** implementing a **button variant system** entirely through `data-variant` values rather than BEM-style modifier classes (`.btn--ghost` as the CSS courseware Module 08 §8.4 would name it) — a `data-*`-driven alternative to BEM that achieves the same visual-variant goal via a different selector mechanism. `ghost` = transparent/outlined secondary button; `danger` = solid red destructive button; `sm` = compact sizing.
- **Line 305–313** — `table { width:100%; border-collapse: collapse; ...; border-radius: var(--radius); overflow: hidden; }` — `border-collapse: collapse` merges adjacent cell borders into single lines (standard table reset, implied but not explicitly named in HTML Module 09's coverage). `overflow: hidden` here is what makes the `border-radius` visually clip the table's corners (matching CSS Module 04 §4.8's explicit callout: "`overflow: hidden` on a card element... clips border-radius").
- **Line 315–329** — `thead`/`th` — sticky-look header row with a slightly different (darker, `var(--bg)`) background than the body rows, small uppercase letter-spaced text — a dense enterprise-data-grid header treatment (conceptually related to HTML Module 09's `scope="col"` header semantics, though that's an HTML attribute concern, not CSS).
- **Line 331–340** — `td`, `tr:last-child > td { border-bottom: none; }` (removes the redundant bottom border on the final row, since the table's own outer border already closes it off), `tbody > tr:hover { background: var(--bg); }` (row-hover highlight for scannability — a **child combinator** ensuring only actual `<tbody>` rows get the hover treatment, not `<thead>` rows).
- **Line 343–365** — The **badge system**: `td > em, span[data-badge]` — a combinator+attribute-selector grouped rule giving both italicized-`<em>`-inside-`<td>` and any `<span data-badge="...">` a shared pill shape (`border-radius: 20px`, small uppercase-ish text). Then a cascade of `span[data-badge="active"]`, `="inactive"`, `="planning"`, `="admin"`, etc. attribute-value selectors assign specific semantic colors per status/role (green for active/completed, red for inactive/on-hold, yellow for planning, purple for admin, blue for manager) — this is a **data-driven color-coding system entirely implemented via CSS attribute selectors reading a single `data-badge` value**, avoiding the need for JS to add different classes per status.
- **Line 368–383** — `nav + main > article > div` — an **adjacent sibling combinator** (`nav + main`, CSS Module 02 §2.6 — "the element immediately following") combined with descendant combinators, precisely targeting the **toolbar row** (search input + filter select) that sits directly above a data table inside `<article>`, without needing to add any extra class to that specific `<div>`. This is a sophisticated, purely-structural selector strategy — it relies on exact DOM position (`nav`'s sibling `main`, its `article` child's first `div`) rather than a class hook, trading some fragility for zero extra markup.
- **Line 386–398** — `nav + main > article > footer` (pagination bar) — the same sibling+descendant strategy applied to a `<footer>` inside `<article>` (page-number controls), using `justify-content: space-between` to spread the "showing X of Y" label to one side and the page-number buttons (wrapped in `footer > div`) to the other.
- **Line 401–413** — `dialog` — sizes and styles the native `<dialog>` element (matching HTML Module 11 §11.5's coverage) with a fixed 480px width capped at `95vw` for mobile safety, and **`dialog::backdrop`** — a pseudo-element **unique to `<dialog>`**, representing the semi-transparent overlay the browser automatically renders behind an open modal (not covered explicitly in the CSS courseware's pseudo-element list in Module 02 §2.5, which covers `::before`/`::after`/`::first-line`/`::placeholder`/`::selection`/`::-webkit-scrollbar` but not `::backdrop` — this is additional modern CSS the project uses beyond the courseware's explicit examples).
- **Line 415–438** — `dialog > header`, `> header > h2`, `> header > button` — styles the modal's title bar as a flex row with the title on the left and a small "✕" close button on the right (`justify-content: space-between`); note `animation: none` (line 423) explicitly **cancels** the `pulse` keyframe animation that would otherwise apply if this `<header>` inadvertently matched the earlier top-level `header` rule (lines 41–56) — a deliberate specificity/override consideration, since `dialog > header` (specificity `0,0,2`) is more specific than the bare `header` type selector (`0,0,1`) and thus correctly wins per the cascade (CSS Module 03 §3.1).
- **Line 440–457** — `dialog > form`, `> form > fieldset` — resets the modal's inner `<form>`/`<fieldset>` layout properties (`position: static`, `background: none`, `border: none`, `width: auto`) to **explicitly cancel out** the very different absolute/fixed styling applied to the *other* `<form>`/`<fieldset>` pair used for the login screen (lines 193–221) — since both forms/fieldsets in this single-page app would otherwise be caught by any looser selector, this file deliberately scopes every form-related rule with specific parent-combinator chains (`body > form` vs `dialog > form`) so the two forms never visually collide. This is a strong practical illustration of why **combinators and selector specificity matter** (CSS Module 02 §2.6, §2.8) in a real single-page app with multiple `<form>` elements serving different purposes.
- **Line 459–467** — `dialog > form > footer` — right-aligns the Cancel/Save buttons (`justify-content: flex-end`) at the bottom of the modal form.
- **Line 470–479** — `aside { position: fixed; bottom:20px; right:20px; display:flex; flex-direction:column; gap:8px; z-index:999; pointer-events:none; }` — the toast-notification container is fixed to the bottom-right corner, stacking toasts vertically with `gap`. `pointer-events: none` on the container (with `pointer-events: auto` restored on the individual `output` children at line 489) is a deliberate technique: the empty space around/between toasts shouldn't block clicks on whatever's underneath, but the toasts themselves should still be interactive (e.g., dismissible).
- **Line 481–496** — `aside > output` and its `data-type` variants (`error`/`success`/`warn`) — each toast is an `<output>` element (HTML's "result of a calculation/user action" element, HTML Module 10 §10.6's `<output>` reused here for notification messages rather than form-calculation results — a repurposing beyond the courseware's narrow example) with `animation: slide-in 0.2s ease` referencing the `@keyframes slide-in` defined at lines 498–501 (transform+opacity entrance animation). Same `data-*`-driven color-variant pattern as the badges and button variants above.
- **Line 504–517** — `main > article > ul` (dashboard stat cards) — `display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));` is the **exact responsive-grid pattern from CSS Module 05 §5.4** ("Responsive card grid: auto-fill creates as many columns as fit") — automatically fits as many 180px-minimum stat cards per row as the container width allows, reflowing without any media query. `list-style: none` strips default bullet styling since this `<ul>` is being repurposed as a card grid rather than a textual list (per HTML Module 07 §7.7, this would ideally also carry `role="list"` in the HTML to preserve list semantics for Safari — not present in this file's markup, a minor accessibility gap).
- **Line 519–542** — `> li > span` (card label), `> li > strong` (the big stat number, using the monospace `--mono` font token for tabular-figure alignment), `> li > small` (secondary caption) — a three-tier typographic hierarchy inside each stat card.
- **Line 545–555** — `main > article > h2` (section headings within the content pane) with `main > article > h2:first-child { margin-top: 0; }` — a **structural pseudo-class** (CSS Module 02 §2.4) removing the top margin only on the very first heading in the pane (so the content doesn't start with unwanted whitespace), while all subsequent `<h2>`s keep their normal top spacing — the same "first-child exception" pattern taught conceptually in CSS Module 06 §6.7 ("Vertical Rhythm... First heading: no top margin").
- **Line 558–563** — `main > article > p` (empty-state message, e.g., "No employees found") — centered, muted, generously padded — a standard empty-state visual treatment.
- **Line 566–598** — `main > article > dl`, `> dt`, `> dd` — a **key-value detail grid** using `display: grid; grid-template-columns: 160px 1fr;` (a fixed-width label column + flexible value column — CSS Module 05 §5.4's "CSS Grid for alignment only" pattern, directly matching the courseware's `.form-grid { grid-template-columns: max-content 1fr; }` example) to lay out `<dl>`/`<dt>`/`<dd>` pairs (e.g., an employee detail view) as clean two-column rows — this is precisely the "underused... perfect for metadata panels" use case HTML Module 07 §7.4 calls out for `<dl>`, now paired with CSS Grid for a professional data-grid look rather than the browser's cramped default `<dl>` rendering. `:last-of-type` (line 595–596, a **structural pseudo-class**, CSS Module 02 §2.4) removes the border on the final row.
- **Line 601–603** — `::-webkit-scrollbar` family — custom thin (6px) scrollbar styling, matching CSS Module 02 §2.5's exact pseudo-element pattern.
- **Line 606–611** — `@media (max-width: 640px) { ... }` — a single **max-width (desktop-first) breakpoint** collapsing the sidebar to a narrow 48px icon-only rail (hiding all text labels via `display:none` on the `<span>` children) and shrinking the modal to `95vw`. Note this is a **max-width** query, whereas the CSS courseware (Module 05 §5.6) recommends a **mobile-first, min-width** approach — this file instead writes full desktop styles as the baseline and uses a single narrowing override for small screens, a legitimate alternative strategy the courseware acknowledges exists but doesn't prefer.
- **Line 615–650 ("Dark mode enhancements")** — Despite the label, this final block does not use `@media (prefers-color-scheme: dark)` (CSS Module 03 §3.5 / Module 08 §8.6's documented technique) — instead it **redundantly re-declares** several rules already established earlier in the file (e.g., `html, body { background: var(--bg); color: var(--text); }` duplicates lines 26–37; `thead { background: #172033; }` overrides the earlier `thead { background: var(--bg); }` with a **hardcoded hex color instead of a token** — a design-token architecture violation per Module 08 §8.3's "primitive vs semantic token" discipline; similarly `tbody > tr:hover`, `input/select/textarea` backgrounds, `button[data-variant="ghost"]`, `dialog` shadow, and `::-webkit-scrollbar-thumb` are all restated with slightly different hardcoded values). Because these later rules share identical or lower specificity with the earlier equivalents and appear **later in source order**, they win per the cascade's "last rule wins" tiebreaker (CSS Module 03 §3.1) — functionally this section acts as a manual "dark theme touch-up" layer appended at the end of the file, but it demonstrates a maintainability anti-pattern relative to the courseware's guidance: hardcoded hex values here bypass the `:root` custom-property system, meaning a future palette change would need to be made in two places instead of one.

---

## Summary of Assessment-Relevant Mechanics to Review

- **Specificity math**: inline (highest) > ID > class/attribute/pseudo-class > element/pseudo-element; equal specificity → last source-order rule wins.
- **Box model**: `content-box` (width excludes padding/border) vs `border-box` (width includes them) — always reset to `border-box` globally.
- **Cascade priority**: `!important` > inline > ID > class > element > inherited > browser default.
- **Inheritance**: `color`, `font-*`, `line-height`, `text-align`, `list-style`, `cursor` inherit; `margin`, `padding`, `border`, `background`, `width/height`, `display`, `position` do not.
- **Flexbox**: `justify-content` = main axis, `align-items` = cross axis; `flex-grow`/`flex-shrink`/`flex-basis` control sizing behavior; `margin-top: auto` / `margin-left: auto` push an item to the far end of a flex container.
- **Grid**: `grid-template-columns: repeat(auto-fill, minmax(X, 1fr))` for responsive columns without media queries; `grid-column: 1 / -1` spans all columns.
- **Positioning**: `relative` establishes a positioning context for `absolute` children; `fixed` is viewport-relative; `sticky` toggles between relative and fixed at a scroll threshold; `z-index` only applies to positioned (non-static) elements.
- **Units**: `rem` for font sizes (no compounding, unlike `em`), `%`/`vw` for fluid widths, `ch` for form-field widths.
- **Accessibility**: every input needs a label; radio/checkbox groups need `<fieldset><legend>`; `alt` required on all `<img>`; `lang` required on `<html>`; focus states must remain visible; one `<h1>` per page with no skipped heading levels.
