from pathlib import Path
from reportlab.lib.colors import HexColor
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle


OUT = Path("output/pdf/Northstar_Financial_Security_Recommendation.pdf")
OUT.parent.mkdir(parents=True, exist_ok=True)

navy = HexColor("#17365D")
blue = HexColor("#2F75B5")
light = HexColor("#EAF2F8")
gray = HexColor("#555555")

styles = getSampleStyleSheet()
title = ParagraphStyle("Title", parent=styles["Title"], fontName="Helvetica-Bold",
                       fontSize=18, leading=22, textColor=navy, spaceAfter=4)
subtitle = ParagraphStyle("Subtitle", parent=styles["Normal"], fontName="Helvetica",
                          fontSize=9.5, leading=12, textColor=gray, spaceAfter=12)
head = ParagraphStyle("Head", parent=styles["Heading2"], fontName="Helvetica-Bold",
                      fontSize=10.5, leading=13, textColor=navy, spaceBefore=7, spaceAfter=3)
body = ParagraphStyle("Body", parent=styles["BodyText"], fontName="Helvetica",
                      fontSize=9.25, leading=12.2, spaceAfter=4)
callout = ParagraphStyle("Callout", parent=body, fontName="Helvetica-Bold",
                         textColor=navy, leading=12.5, spaceAfter=0)

story = []
story.append(Paragraph("Customer Security Recommendation", title))
story.append(Paragraph("Northstar Financial | Account-update authorization pull request | Release planned in three days", subtitle))

decision = Table([[Paragraph(
    "<b>Release decision:</b> The engineering director, advised by the AppSec lead and service owner, must decide whether this authorization change is ready to release. The workflow is currently stuck because the PR has evidence of a missing authorization control but not enough proof to approve or reject the release safely.", callout)
]], colWidths=[6.5*inch])
decision.setStyle(TableStyle([
    ("BACKGROUND", (0, 0), (-1, -1), light), ("BOX", (0, 0), (-1, -1), 0.6, blue),
    ("LEFTPADDING", (0, 0), (-1, -1), 10), ("RIGHTPADDING", (0, 0), (-1, -1), 10),
    ("TOPPADDING", (0, 0), (-1, -1), 8), ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
]))
story += [decision, Spacer(1, 5)]

sections = [
    ("Recommended first step",
     "Perform a focused, read-only authorization review of the PR, the directly related authorization files, and their tests. Trace the account-update path, compare the removed role-checking helper with the prior behavior, and assess whether tests enforce customer-to-account ownership. This is the highest-value first step because it targets the imminent release and uses the AppSec lead's single review slot; reviewing every repository would dilute attention, exceed authorization, and would not answer this release decision."),
    ("Scope and accountable reviewers",
     "In scope: the authorization-related PR, directly related files, and directly related tests in customer-account-service. Out of scope: other repositories, production testing, exploit reproduction, third-party systems, remediation, merge, and an automated release decision. The service owner/developer remains accountable for intended authorization behavior and any code change; the AppSec lead remains accountable for security assessment and risk advice; the engineering director remains accountable for the release decision."),
    ("Evidence position and classification",
     "Directly supported: the changed path no longer calls the previously present role-checking helper, and existing tests show an authenticated user can update an account but do not show that one customer cannot update another customer's account. A reasonable inference is that this may permit an authorization bypass or ownership-check regression. Classify it as a high-priority, evidence-backed authorization concern requiring human validation before release. It is not proven that cross-customer updates are possible, exploitable, reachable in production, or that a vulnerability exists; no runtime validation or exploit reproduction was performed."),
    ("Success measures and guardrails",
     "Useful output is a concise finding with the changed code path, removed control, test gap, confidence and limitations, plus a developer-verifiable recommendation. Preserve read-only access, approved scope, human triage, no automatic fixes or merges, no production activity, and no automated security or release guarantee."),
    ("Next action and owner",
     "The AppSec lead should review the focused output with the service owner immediately and request an ownership-authorization test or an explanation of the compensating control. The engineering director then uses the documented evidence and reviewer judgment to decide whether to hold the PR pending validation, accept a documented risk, or release after the concern is resolved and re-reviewed.")
]

for h, text in sections:
    story.append(Paragraph(h, head))
    story.append(Paragraph(text, body))

doc = SimpleDocTemplate(str(OUT), pagesize=letter, rightMargin=0.75*inch, leftMargin=0.75*inch,
                        topMargin=0.62*inch, bottomMargin=0.58*inch, title="Northstar Financial Security Recommendation")
doc.build(story)
print(OUT.resolve())
