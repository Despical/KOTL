# Security Policy

Security reports are taken seriously. If you find a vulnerability in MusicBot,
please report it privately instead of opening a public issue.

## Reporting a Vulnerability

Please send security reports to:

```text
contact@despical.dev
```

When possible, include the following details:

* A clear description of the vulnerability.
* Steps to reproduce the issue.
* The affected version, commit, branch, or deployment environment.
* Any relevant logs, screenshots, command examples, or proof of concept details.
* Whether the issue appears to affect bot authentication, Discord permissions,
  voice connections, guild state storage, or external API credentials.

Please do not include destructive payloads, real user data, private credentials,
or anything that could damage a running deployment.

## Scope

The following areas are considered security-sensitive:

* Discord bot token handling and environment variable loading.
* Spotify client credentials and API access token handling.
* Slash command permission assumptions and guild-only command behavior.
* Voice channel connection checks and playback control authorization.
* Local state storage in `data/guild-state.json`.
* External media URL handling and source resolution.
* Dependency vulnerabilities in JDA, LavaPlayer, JDave, Jackson, and Gradle plugins.

Reports about spam, Discord server moderation, audio quality preferences, or
non-security bugs should use the normal GitHub issue tracker instead.

## Supported Versions

Only the latest public version of MusicBot is currently supported. If you are
running an older version, please update before reporting unless the same issue
also exists on the latest version.

## Response

After a valid report is received, the issue will be reviewed as soon as possible.
If the report is confirmed, a fix will be prepared privately and released with
credit where appropriate.

Please avoid public disclosure until a fix is available.
