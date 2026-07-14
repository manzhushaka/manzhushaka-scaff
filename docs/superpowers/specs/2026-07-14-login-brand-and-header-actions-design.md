# Login Brand And Header Actions Design

## Goal

Align the `manzhushaka-scaff` login page with the `hnums-bi-console` split-screen
layout while branding the application as `manzhushaka - scaff`. Replace the
application logo and browser icon with the supplied red spider lily image. Remove
the requested global header shortcuts and their unused implementation code.

## Scope

### Login page

- Replace `ui-admin/src/views/login.vue` with the proven two-column layout used by
  `hnums-bi-console`: an informational left panel and an authentication right panel.
- Keep the existing login API, validation, CAPTCHA, remember-password cookie, redirect,
  registration toggle, and loading behavior unchanged.
- Use `manzhushaka - scaff` as the visible product name. Keep supporting copy concise
  and generic so it does not claim a business domain absent from this scaffold.
- Preserve the responsive behavior: two columns on desktop and stacked panels on narrow
  screens.

### Brand assets

- Derive web-sized PNG assets from the supplied 2048px spider lily image.
- Use the brand asset in the login introduction and in the existing sidebar logo slot.
- Replace `ui-admin/public/favicon.ico` with a browser-compatible icon derived from the
  same image. No external image URL or runtime image dependency is introduced.

### Header action removal

- Remove these desktop header entries from `ui-admin/src/layout/components/Navbar.vue`:
  global menu search, source-code link, documentation link, browser fullscreen, and
  layout-size selector.
- Remove their corresponding imports. Delete component source files that have no
  remaining consumers: `HeaderSearch`, `Screenfull`, `SizeSelect`, and the two
  `RuoYi` external-link components.
- Retain the hamburger control, breadcrumbs, account avatar menu, profile link, layout
  settings menu item, screen lock, and logout. The requested removal of the layout-size
  selector does not remove the separate layout-settings drawer.
- Keep `RightToolbar` and per-page search/filter controls because they are business-page
  functions, not the global header search.
- Keep tag-view fullscreen behavior unless it is reachable from a separate visible
  control; it is not the requested top-right fullscreen entry.

## Implementation Boundaries

The change is frontend-only. Backend authentication routes, data models, SQL, permissions,
and menu initialization remain untouched. No new packages are required.

## Validation

- Run `npm run build:prod` from `ui-admin`.
- Start the Vite development server and visually inspect the desktop and mobile login
  layouts.
- Verify the right side of the authenticated header contains only the account menu.
- Verify CAPTCHA refresh, validation errors, remember-password behavior, and successful
  login navigation still work.

## Success Criteria

- The login page clearly matches the reference page's structural layout while displaying
  `manzhushaka - scaff` and the supplied spider lily asset.
- Sidebar and browser tab use the same new brand image.
- The five requested header shortcuts are absent, and their now-unreferenced component
  JavaScript is removed without affecting unrelated search or settings functionality.
