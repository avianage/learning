from reportlab.lib.pagesizes import landscape, A4
from reportlab.pdfgen import canvas
from reportlab.lib.colors import HexColor, white
from reportlab.pdfbase.pdfmetrics import stringWidth
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfbase import pdfmetrics
from reportlab.lib.units import mm
import os

OUT = r"output/pdf/Codex_Enterprise_Rollout_Design_Linton_Labs.pdf"
PAGE_W, PAGE_H = landscape(A4)

# Palette: deep ink, enterprise blue, mint signal, warm accent
INK = HexColor('#0B1F33')
NAVY = HexColor('#102B46')
BLUE = HexColor('#2577B7')
PALE_BLUE = HexColor('#EAF3FA')
MINT = HexColor('#D9F0E8')
MINT_DARK = HexColor('#0E6B5B')
WARM = HexColor('#F6EEE4')
AMBER = HexColor('#A65A08')
LINE = HexColor('#C9D7E2')
MUTED = HexColor('#52677A')

FONT = 'Helvetica'
BOLD = 'Helvetica-Bold'

def text(c, x, y, s, size=7.3, color=INK, font=FONT):
    c.setFont(font, size); c.setFillColor(color); c.drawString(x, y, s)

def wrap(c, s, width, size=7.3, font=FONT):
    words=s.split(); lines=[]; cur=''
    for w in words:
        nxt=(cur+' '+w).strip()
        if stringWidth(nxt, font, size) <= width: cur=nxt
        else:
            if cur: lines.append(cur)
            cur=w
    if cur: lines.append(cur)
    return lines

def para(c, x, y, s, width, size=7.1, leading=8.6, color=INK, font=FONT, bullet=False):
    lines=wrap(c,s,width-(8 if bullet else 0),size,font)
    for i,line in enumerate(lines):
        if bullet and i==0:
            c.setFillColor(BLUE); c.circle(x+2,y+2,1.25,fill=1,stroke=0)
        text(c,x+(8 if bullet else 0),y,line,size,color,font)
        y-=leading
    return y

def section(c, x, top, w, title, items, tint):
    header=17
    # Calculate contents first
    heights=[]
    for item in items:
        heights.append(max(17, len(wrap(c,item,w-20,6.55,FONT))*7.55+5))
    h=header+sum(heights)+6
    c.setFillColor(white); c.roundRect(x, top-h, w, h, 5, fill=1, stroke=0)
    c.setStrokeColor(LINE); c.roundRect(x, top-h, w, h, 5, fill=0, stroke=1)
    c.setFillColor(tint); c.roundRect(x,top-header,w,header,5,fill=1,stroke=0)
    c.rect(x,top-header,w,5,fill=1,stroke=0)
    text(c,x+8,top-11,title.upper(),7.3,INK,BOLD)
    y=top-header-9
    for item, ih in zip(items,heights):
        y=para(c,x+7,y,item,w-14,6.55,7.55,INK,FONT,True)
        y-=2
    return top-h

c=canvas.Canvas(OUT,pagesize=landscape(A4),pageCompression=1)
c.setTitle('Codex Enterprise Rollout Design - Linton Labs')
c.setAuthor('Linton Labs | Rollout Review')
c.setFillColor(HexColor('#F7FAFC')); c.rect(0,0,PAGE_W,PAGE_H,fill=1,stroke=0)

# Masthead
c.setFillColor(NAVY); c.rect(0,PAGE_H-60,PAGE_W,60,fill=1,stroke=0)
c.setFillColor(MINT); c.circle(30,PAGE_H-29,11,fill=1,stroke=0)
c.setFillColor(MINT_DARK); c.setFont(BOLD,12); c.drawCentredString(30,PAGE_H-33,'C')
text(c,52,PAGE_H-25,'CODEX ENTERPRISE ROLLOUT DESIGN',17,white,BOLD)
text(c,52,PAGE_H-42,'Linton Labs - staged launch: Billing Platform first, then proven patterns at scale',8.2,HexColor('#C9DDED'))
c.setFillColor(HexColor('#17496E')); c.roundRect(PAGE_W-202,PAGE_H-45,176,23,11,fill=1,stroke=0)
text(c,PAGE_W-191,PAGE_H-36,'RECOMMENDATION  |  SAFE, ADOPTABLE, AUDITABLE',6.5,white,BOLD)

# Executive strip
strip_y=PAGE_H-73
c.setFillColor(PALE_BLUE); c.roundRect(18,strip_y-28,PAGE_W-36,28,5,fill=1,stroke=0)
text(c,28,strip_y-11,'Decision standard:',7.1,BLUE,BOLD)
text(c,98,strip_y-11,'every default has an owner, an enforcement point, a verification command, and a recovery route - no tribal knowledge required.',7.1,INK)

gap=8; margin=18; colw=(PAGE_W-2*margin-2*gap)/3
x1=margin; x2=x1+colw+gap; x3=x2+colw+gap; top=PAGE_H-110

bottom1=section(c,x1,top,colw,'1. Surface strategy',[
'DECISION 1 - Billing defaults to the Codex app for task orchestration and reviewable handoffs. IDE extension is for small local edits; CLI is for repeatable/scripted or CI-adjacent work. Cloud is a governed exception for eligible long-running, non-sensitive tasks.',
'DECISION 2 - Worktrees are mandatory for payments, migrations, shared libraries, release branches, and parallel tasks; optional only for isolated docs, tests, and single-file experiments.',
'DECISION 3 - Cloud is required for approved long-running parallel work that benefits from managed isolation; forbidden for production access, customer/regulated data, unapproved secrets, incident response, or repos without cloud eligibility.',
'GUARDRAIL 1 - Windows sandbox defaults to unelevated. Elevated mode is only for vetted setup needing installs/drivers plus an approved admin path. Use WSL semantics for Linux-first tooling. If elevated setup is blocked, use unelevated + prebuilt dev image/remote runner or WSL; do not bypass controls.'
],PALE_BLUE)

bottom2=section(c,x2,top,colw,'2. Model strategy',[
'DECISION 4 - Default to GPT-5.6 Terra for most engineering work. API-key workflows use the current API flagship GPT-5.6 (gpt-5.6-sol) for complex coding/reasoning; pin tested snapshots where repeatability matters. Revalidate availability at rollout time.',
'Escalate reasoning from medium to high/xhigh for cross-repo changes, ambiguous failures, migrations, security-sensitive review, or when two bounded attempts fail. Keep routine edits and known test fixes at lower effort.',
'Spark: use only for ultra-low-latency, narrow edit-test loops with a small diff and deterministic checks. Do not use it for design, multi-file refactors, security, migrations, or tasks needing broad context.',
'Spark guardrail: tighten scope to one intent, require explicit target files, run the relevant test/lint command, and have a human inspect the diff before merge.'
],MINT)

bottom3=section(c,x3,top,colw,'3. Context continuity',[
'DECISION 5 - Checkpoint at each completed subgoal, before risky commands, after a material decision, every 20-30 active minutes, and before switching surfaces. Record goal, decisions, changed files, verification, next command, and blockers in the task template.',
'Compact only after writing the checkpoint. Preserve accepted/rejected alternatives, constraints, exact environment/sandbox state, and links to PR/issue/logs; remove only redundant tool chatter.',
'RECOVERY A - failed attempt: capture error + revert only owned changes -> retry once with a narrower hypothesis -> escalate to human. RECOVERY B - drift: re-read instructions/diff and restore the stated goal. RECOVERY C - surface switch/restart: open the checkpoint, confirm git status and environment, then continue in a new thread if context is compromised.'
],WARM)

top2=min(bottom1,bottom2,bottom3)-9
bottom4=section(c,x1,top2,colw,'4. Governance bundle',[
'Repo root: version AGENTS.md with verification commands, review guidance, ownership boundaries, and a concrete “done” definition (tests green, diff reviewed, docs/changelog as required, no untracked secrets). Store organization defaults in managed config.toml; repo-specific overrides stay checked in and reviewed.',
'GUARDRAIL 2 - .rules allowlists safe build/test/git-read commands; approval is required for writes outside the workspace, package installs, network, credentials, privileged operations, destructive git, and production tooling.',
'Publish managed skills for secure PR review, CI fix loop, release notes, dependency updates, and incident-safe triage. Version, test, and assign owners to each skill.'
],PALE_BLUE)

bottom5=section(c,x2,top2,colw,'5. Cloud + integrations',[
'GUARDRAIL 3 - Cloud environments use a minimal pinned setup script and lockfile-hash caches. Secrets come only from a secrets manager with short-lived, task-scoped credentials - never repo files, prompts, or durable plain environment variables. GUARDRAIL 4 - Internet is off by default; enable only destination allowlists per task.'
'GitHub: start with manual @codex review for Billing Platform, required human approval, and sampling-based calibration. Enable automatic reviews only after quality, false-positive, and access thresholds are met.',
'Slack/Linear: delegate status drafting, issue summaries, acceptance-criteria proposals, and PR/CI links. Never delegate approvals, incident command, production changes, customer commitments, security exceptions, or external posting without a named human owner.'
],MINT)

bottom6=section(c,x3,top2,colw,'6. Admin + monitoring',[
'RBAC: separate local user, cloud-eligible developer, repo steward, auditor, and admin groups. Start Billing Platform with a small champion cohort; grant cloud only to approved groups and repositories.',
'Track analytics: adoption, surface/model mix, task completion, latency, retry/failure rate, review acceptance, and support volume. Track compliance logs separately: identity, repo/environment, approvals, sandbox mode, network/secrets policy events, tool actions, and retention/export access.',
'Support runbook: inspect approval history, sandbox mode, selected environment/image, policy match, identity/sign-in entitlement, repo/cloud eligibility, and missing GitHub/Slack/Linear permissions. Windows triage: verify elevated prerequisites, active unelevated/elevated sandbox mode, WSL availability if selected, and corporate sign-in/entitlement path.'
],WARM)

# Footer with rollout gates
c.setFillColor(NAVY); c.roundRect(18,16,PAGE_W-36,28,5,fill=1,stroke=0)
text(c,28,33,'ROLLOUT GATES',6.8,HexColor('#A9D5F2'),BOLD)
text(c,103,33,'1  Billing pilot: local + worktrees + manual review',6.7,white)
text(c,294,33,'2  Expand after policy/test evidence',6.7,white)
text(c,449,33,'3  Cloud + automation only for eligible cohorts',6.7,white)
text(c,28,23,'Owner: Platform Engineering  |  Review cadence: weekly during pilot; monthly after scale-out  |  Decision log: attach to rollout PRD',6.2,HexColor('#D6E5F0'))
c.showPage(); c.save()
print(OUT)
