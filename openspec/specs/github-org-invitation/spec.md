## Requirements

### Requirement: Multiple GitHub Apps management
The system SHALL support multiple GitHub Apps with unified configuration structure while sharing common JWT and token infrastructure.

The configuration SHALL use a hierarchical structure under `github.apps.{app-name}`:
- Each app SHALL have standard fields: `app-id`, `private-key-path`, `type`, `enabled`
- `type` SHALL be either `repository` or `organization` to determine installation URL strategy
- App-specific fields SHALL be nested under the app name (e.g., `owner`/`repo` for repository type, `org` for organization type)

The system SHALL:
- Share JWT generation and private key loading logic across all apps
- Determine installation URL based on app type: `/repos/{owner}/{repo}/installation` for repository type, `/orgs/{org}/installation` for organization type
- Allow each app to be independently enabled/disabled

#### Scenario: Issue sync app configuration
- **WHEN** the Issue sync GitHub App is configured under `github.apps.issue-sync`
- **THEN** it SHALL use `type: repository` with `owner` and `repo` fields
- **AND** it SHALL function independently of other GitHub Apps

#### Scenario: Organization invitation app configuration
- **WHEN** the organization invitation GitHub App is configured under `github.apps.org-invitation`
- **THEN** it SHALL use `type: organization` with `org` field
- **AND** it SHALL support additional fields like `team-mapping`

#### Scenario: Shared token infrastructure
- **WHEN** any GitHub App needs an installation access token
- **THEN** the system SHALL use shared JWT generation logic
- **AND** the installation URL SHALL be determined by the app's configured type

#### Scenario: Independent app enablement
- **WHEN** one GitHub App is disabled via its `enabled` flag
- **THEN** other GitHub Apps SHALL continue to function normally

### Requirement: GitHub organization invitation service
The system SHALL provide a service to invite users to the Blue-Net-Team GitHub organization.

The service SHALL:
- Use a dedicated GitHub App with `Organization members: Read and write` permission
- Obtain installation access token via JWT authentication
- Invite users by `invitee_id` (GitHub user ID) when available, or by `email` as fallback
- Assign users to direction-specific GitHub Teams based on their `direction` field
- Map backend directions to GitHub team names via configuration

#### Scenario: Invite user with GitHub binding
- **WHEN** inviting a user who has `githubId` bound
- **THEN** the system SHALL use `invitee_id` parameter with the user's GitHub ID
- **AND** assign the user to the team matching their direction

#### Scenario: Invite user without GitHub binding
- **WHEN** inviting a user who has no `githubId` but has `email`
- **THEN** the system SHALL use `email` parameter with the user's email address
- **AND** assign the user to the team matching their direction

#### Scenario: Invite user without GitHub binding and email
- **WHEN** inviting a user who has neither `githubId` nor `email`
- **THEN** the system SHALL skip the invitation
- **AND** log a warning message

#### Scenario: User already in organization
- **WHEN** GitHub API returns 422 (user already invited or already a member)
- **THEN** the system SHALL log the result as informational
- **AND** NOT treat it as an error

#### Scenario: Direction to team mapping
- **WHEN** the system needs to assign a user to a team
- **THEN** it SHALL look up the team name from configuration based on the user's direction
- **AND** resolve the team name to team ID via GitHub API
- **AND** include the team ID in the invitation request

### Requirement: Automatic invitation on assessment pass
The system SHALL automatically send a GitHub organization invitation when a candidate passes the global final assessment and is promoted to MEMBER role.

The invitation SHALL:
- Be triggered asynchronously after role promotion
- Not block or fail the role promotion process
- Not block or fail the email notification
- Log failures without throwing exceptions

#### Scenario: Candidate passes global final assessment
- **WHEN** a candidate's decision is published for the global final assessment
- **AND** the decision is `passed = true`
- **AND** the candidate's current role is CANDIDATE
- **THEN** the system SHALL promote the candidate to MEMBER role
- **AND** asynchronously send a GitHub organization invitation
- **AND** send the decision email notification regardless of invitation result

#### Scenario: GitHub invitation failure is non-blocking
- **WHEN** the GitHub organization invitation fails (network error, API error, etc.)
- **THEN** the system SHALL log the failure
- **AND** the candidate's role promotion SHALL still succeed
- **AND** the email notification SHALL still be sent

#### Scenario: Non-final assessment does not trigger invitation
- **WHEN** a candidate passes a non-final assessment round
- **THEN** the system SHALL NOT send a GitHub organization invitation

### Requirement: Manual single user invitation
The system SHALL allow administrators to manually invite a single user to the GitHub organization via API.

#### Scenario: Admin invites single user
- **WHEN** an administrator calls the invitation API with a valid user ID
- **THEN** the system SHALL send a GitHub organization invitation to that user
- **AND** return the invitation result

#### Scenario: Admin invites user without GitHub binding or email
- **WHEN** an administrator calls the invitation API for a user with no GitHub binding and no email
- **THEN** the system SHALL return an error indicating the user cannot be invited

#### Scenario: Unauthorized user cannot invite
- **WHEN** a non-administrator attempts to call the invitation API
- **THEN** the system SHALL reject the request with a forbidden response

### Requirement: Manual batch invitation
The system SHALL allow administrators to invite multiple users in a single request.

#### Scenario: Admin batch invites users
- **WHEN** an administrator calls the batch invitation API with a list of user IDs
- **THEN** the system SHALL attempt to invite each user
- **AND** return a summary with total count, succeeded count, failed count, and per-user details

#### Scenario: Batch invitation with mixed results
- **WHEN** a batch invitation contains users that can and cannot be invited
- **THEN** the system SHALL process all users
- **AND** return individual results for each user with `userId`, `success`, and `reason` fields

#### Scenario: Batch size limit
- **WHEN** an administrator submits a batch larger than the maximum allowed size
- **THEN** the system SHALL reject the request with a validation error

### Requirement: Admin invitation management page
The system SHALL provide an admin page for managing GitHub organization invitations.

The page SHALL:
- Display users in card or table view (toggleable)
- Show each user's name, email, GitHub binding status, direction, and role
- Provide a button to invite a single user
- Provide batch selection and batch invitation capability
- Display invitation results (success/failure with reason)

#### Scenario: Admin views invitation page
- **WHEN** an administrator navigates to the GitHub invitations page
- **THEN** the system SHALL display a list of users with their GitHub binding status and direction

#### Scenario: Admin invites single user from page
- **WHEN** an administrator clicks the invite button for a user
- **THEN** the system SHALL call the invitation API
- **AND** display the result (success or failure with reason)

#### Scenario: Admin batch invites users from page
- **WHEN** an administrator selects multiple users and clicks batch invite
- **THEN** the system SHALL call the batch invitation API
- **AND** display a summary of results

#### Scenario: Page access control
- **WHEN** a non-administrator attempts to access the page
- **THEN** the system SHALL deny access
