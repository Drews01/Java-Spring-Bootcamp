# Manual OWASP Top 10 Testing Guide

This guide explains how to manually test the application against the OWASP Top 10 risks using two tooling options:

1. **Option A – Burp Suite (Community or Professional)**
2. **Option B – OWASP ZAP**

Each option covers the same checklist so security reviewers can choose their preferred tooling without losing coverage.

---

## Test Scope

- **Application Under Test (AUT):** Spring Boot API + UI delivered by this repository.
- **Environments:** Prefer a staging environment mirroring production data models but containing sanitized data. Never test against production without explicit approval.
- **Prerequisites:**
  - Running application instance (e.g., `mvn spring-boot:run` or `./run-app.bat`).
  - Valid test credentials for each role (admin, manager, user, etc.).
  - Proxy-aware browser (Firefox/Chrome) configured to trust the interception tool's certificate.

---

## OWASP Top 10 Checklist

| ID | Risk | Manual Checks |
| --- | --- | --- |
| A01 | Injection | Fuzz query/body params, check server-side validation, ensure repository methods use parameterized queries, attempt Redis command injection. |
| A02 | Broken Authentication | Attempt brute force (with throttling disabled), verify password policies, ensure JWT/session invalidation on logout, test MFA hooks if present. |
| A03 | Sensitive Data Exposure | Confirm HTTPS/HSTS, inspect responses/logs for PII, verify DB/Redis encryption and secret handling. |
| A04 | XXE | Identify XML parsers, ensure external entities disabled, send crafted XML payloads. |
| A05 | Broken Access Control | Replay requests with downgraded roles, manipulate JWT claims/scopes, ensure `@PreAuthorize` is enforced, inspect cached Redis entries for leakage. |
| A06 | Security Misconfiguration | Review HTTP headers (CSP, X-Frame-Options), check actuator endpoints, confirm Redis not exposed publicly, validate CORS rules. |
| A07 | XSS | Inject script payloads into every input, confirm server-side encoding, test reflected/stored vectors. |
| A08 | Insecure Deserialization | Attempt gadget payloads through serialized fields or cached objects, verify deserializer allow-lists. |
| A09 | Components with Known Vulnerabilities | Compare dependency versions via SBOM/dependency-check reports, verify patched Redis/Spring Boot versions. |
| A10 | Logging & Monitoring | Trigger auth failures, privilege escalations, Redis errors and ensure they appear in centralized logs/SIEM with alerts. |

Use the relevant tool actions below to exercise each checklist item.

---

## Option A – Burp Suite Workflow

1. **Setup**
   - Launch Burp Suite and configure your browser proxy to `127.0.0.1:8080`.
   - Import Burp's CA certificate into the browser trust store for HTTPS interception.
   - Add the AUT host to "Target → Scope" to focus scans.

2. **Crawl & Discover**
   - Use the built-in browser or "Proxy → HTTP history" to navigate through the UI/API, capturing requests for all roles.
   - Enable "Engagement tools → Content discovery" for hidden endpoints.

3. **Manual Testing per OWASP Category**
   - **Injection:** Send critical requests to Repeater/Intruder. Insert payload lists (SQL, NoSQL, Redis command injections). Observe error messages, DB slowdowns, or unexpected behavior.
   - **Broken Authentication:** In Intruder, launch cluster bomb attacks on login with username/password lists (only in staging). Check whether account lockout or rate limiting triggers. Inspect JWT/session cookies for predictable patterns.
   - **Sensitive Data Exposure:** Review responses in Proxy/Logger for secrets. Use Decoder to analyze tokens. Capture TLS details via Burp's SSL tab.
   - **XXE:** Craft XML bodies in Repeater containing `<!DOCTYPE>` payloads and observe for out-of-band callbacks (use Burp Collaborator).
   - **Broken Access Control:** Modify JWT claims (e.g., `role`), remove security headers, or replay admin endpoints with user tokens to confirm `403` responses.
   - **Security Misconfiguration:** Run the Burp passive scanner to flag missing headers, open directories, or default credentials.
   - **XSS:** Use the Burp "XSS cheat sheet" payloads in Intruder or Repeater, then render responses through the browser to detect execution.
   - **Insecure Deserialization:** Replace serialized fields (Base64/JSON) with gadget payloads or unexpected class names; monitor server logs for exceptions.
   - **Components with Known Vulnerabilities:** Import the project's SBOM or dependency list into Burp's Vulnerability scanner module (Pro) or correlate manually with advisories.
   - **Logging & Monitoring:** Observe whether deliberately malformed requests generate traceable entries (coordinate with ops team).

4. **Reporting**
   - Use "Issue activity" to document confirmed findings with screenshots/request-response pairs.
   - Export HTML or XML reports for tracking.

---

## Option B – OWASP ZAP Workflow

1. **Setup**
   - Start ZAP and set browser proxy to `127.0.0.1:8080` (or ZAP's configured port).
   - Install ZAP's root certificate into the browser.
   - Configure contexts for the AUT ("Context → Include in context"). Assign authentication scripts if needed.

2. **Spidering & Passive Scan**
   - Run the traditional spider for web UI and the AJAX spider for dynamic content.
   - Enable the API scanner (`zap-api-scan.py`) if you have an OpenAPI/Swagger definition.
   - Review passive scan alerts for missing headers, insecure cookies, etc.

3. **Active Testing per OWASP Category**
   - **Injection:** Use "Active Scan" with policies focused on SQL/command injections. Manually craft attacks in "Request Editor" for Redis-specific payloads.
   - **Broken Authentication:** Configure Forced Browse and Fuzzer plugins to attempt weak passwords, session fixation, and token replay. Monitor "Session Properties" for cookie issues.
   - **Sensitive Data Exposure:** Inspect the "History" tab for leaked tokens. Run the "Reveal hidden fields" script and check TLS info via "Quick Start → Automated Scan" results.
   - **XXE:** Utilize the "XML External Entity" scan rule; supplement with manual crafted requests in the Request tab.
   - **Broken Access Control:** Use "User" contexts with different roles. Send unauthorized requests via the "Manual Request" editor and verify responses.
   - **Security Misconfiguration:** Review alerts for directory listing, missing headers, and open CORS policies. Use the "Baseline Scan" for regressions.
   - **XSS:** Execute the "Cross-Site Scripting" active scan rule set; confirm findings manually in the browser tab.
   - **Insecure Deserialization:** Apply custom scripts (e.g., "Fuzzer → Payload processors") to mutate serialized objects and watch server reactions.
   - **Components with Known Vulnerabilities:** Import dependency reports into ZAP's "Add-on: Software Composition Analysis" (if enabled) or correlate alerts with dependency versions.
   - **Logging & Monitoring:** Send malformed/attack traffic, then coordinate with ops to ensure logs show entries; ZAP's "Request/Response" pairs can be attached to tickets.

4. **Automation Hooks**
   - Save the context and scan policy for repeat runs.
   - Integrate `zap-baseline.py` (passive) or `zap-full-scan.py` (active) into CI to complement manual efforts.

5. **Reporting**
   - Use "Report → Generate Report" (HTML/Markdown) with filters per risk level.
   - Attach ZAP alerts to issue trackers with remediation notes referencing Spring Boot, Redis, or infrastructure fixes.

---

## Evidence & Remediation Tracking

- Log every finding with: request sample, response sample, impacted OWASP ID, severity, remediation recommendation, and test date.
- Update `PROJECT_DOCUMENTATION.md` or ticketing system with the status of each OWASP category after every testing cycle.
- Re-run targeted tests after any patch impacting authentication, authorization, serialization, or caching layers.
