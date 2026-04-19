## 1. Dependencies and Shared Components

- [x] 1.1 Add Markdown rendering dependencies to the frontend package.
- [x] 1.2 Create a shared sanitized `MarkdownRenderer` component with dark-theme styles for paragraphs, lists, links, tables, inline code, and fenced code blocks.
- [x] 1.3 Create a reusable question stem Markdown editor component that supports live preview, empty preview state, desktop side-by-side layout, and mobile edit/preview tabs.

## 2. Admin Question Drawer

- [x] 2.1 Replace file upload question stem textarea with the Markdown editor while preserving the existing form field name and payload shape.
- [x] 2.2 Replace single choice and multiple choice question stem textareas with the Markdown editor while preserving option and answer behavior.
- [x] 2.3 Replace algorithm question stem textarea with the Markdown editor while preserving test case, time limit, and memory limit behavior.
- [x] 2.4 Render question stems through `MarkdownRenderer` in drawer view mode.
- [x] 2.5 Make create/edit drawer width responsive: wide on desktop and full-screen on mobile.

## 3. Candidate Question Detail Page

- [x] 3.1 Replace file upload question stem line-splitting logic with Markdown rendering of `content.content`.
- [x] 3.2 Keep allowed file type and max file size metadata outside the Markdown document as separate metadata rows.
- [x] 3.3 Verify existing plain text question stems remain readable in the candidate view.

## 4. Verification

- [x] 4.1 Verify Markdown headings, lists, links, tables, inline code, and fenced code blocks render correctly in admin preview and candidate detail pages.
- [x] 4.2 Verify raw HTML or script-like Markdown input is not executed in admin preview, admin view mode, or candidate view.
- [x] 4.3 Run frontend lint or build checks available for the project.
- [x] 4.4 Manually inspect desktop and mobile layouts for the admin drawer editing experience.
