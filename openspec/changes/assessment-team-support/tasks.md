## 1. Database Migration

- [x] 1.1 Create Flyway migration: add `allow_team` to `tb_assessment_time`, add `team_id` to `tb_assessment_answer`
- [x] 1.2 Create Flyway migration: create `tb_assessment_team` and `tb_assessment_team_member` tables

## 2. Domain Layer (Backend)

- [x] 2.1 Add `allowTeam` field to `AssessmentTime` entity with create/update behavior
- [x] 2.2 Add `teamId` field to `AssessmentAnswer` entity
- [x] 2.3 Create `AssessmentTeam` domain entity with fields: id, assessmentTimeId, leaderId, name, inviteCode, status
- [x] 2.4 Create `AssessmentTeamMember` domain entity/value object
- [x] 2.5 Create `AssessmentTeamRepository` interface with methods: save, findById, findByAssessmentTimeIdAndUserId, findByInviteCode, delete, updateLeader, addMember, removeMember

## 3. Application Layer (Backend)

- [ ] 3.1 Create `AssessmentTeamAppService` interface with methods: createTeam, previewTeam, joinTeam, getMyTeam, leaveTeam, transferLeader, disbandTeam
- [ ] 3.2 Implement `AssessmentTeamAppServiceImpl.createTeam` with invite code generation and leader assignment
- [ ] 3.3 Implement `AssessmentTeamAppServiceImpl.previewTeam` (side-effect free preview by invite code)
- [ ] 3.4 Implement `AssessmentTeamAppServiceImpl.joinTeam` with validation (not in other team, no personal answer, time not ended)
- [ ] 3.5 Implement `AssessmentTeamAppServiceImpl.getMyTeam` returning team info with member list (reuse UserInfo)
- [ ] 3.6 Implement `AssessmentTeamAppServiceImpl.leaveTeam` (leader cannot leave without transfer)
- [ ] 3.7 Implement `AssessmentTeamAppServiceImpl.transferLeader` (validate target is team member)
- [ ] 3.8 Implement `AssessmentTeamAppServiceImpl.disbandTeam` (leader only)
- [ ] 3.9 Modify `AssessmentTimeAppServiceImpl.listAssessmentTimesForUser` to include `direction IS NULL` results
- [ ] 3.10 Modify `AssessmentTimeAppServiceImpl.createAssessmentTime` to accept and save `allowTeam`
- [ ] 3.11 Modify `AssessmentTimeAppServiceImpl.updateAssessmentTime` to accept and update `allowTeam`
- [ ] 3.12 Modify `AssessmentAnswerAppServiceImpl.createAnswer` to validate team/leader permission for FILE_UPLOAD in team-enabled assessments
- [ ] 3.13 Modify `AssessmentAnswerAppServiceImpl.updateAnswer` to validate team/leader permission for FILE_UPLOAD in team-enabled assessments
- [ ] 3.14 Modify `AssessmentAnswerAppServiceImpl.getMyAnswer` to return leader's answer for team members on FILE_UPLOAD questions
- [ ] 3.15 Modify `AssessmentAnswerAppServiceImpl.validateDirectionMatch` to allow `time.direction == null` (cross-direction)

## 4. Infrastructure Layer (Backend)

- [x] 4.1 Create `AssessmentTeamDO` and `AssessmentTeamMemberDO` data objects
- [x] 4.2 Create `AssessmentTeamMapper` interface and XML with CRUD operations
- [x] 4.3 Create `AssessmentTeamMemberMapper` interface and XML
- [x] 4.4 Implement `AssessmentTeamRepositoryImpl` with DO conversion
- [x] 4.5 Create repository converters for team entities
- [x] 4.6 Modify `AssessmentTimeMapper.xml` `selectPageByUserParticipation` to support `direction IS NULL`
- [x] 4.7 Modify `AssessmentAnswerMapper.xml` to support `team_id` field

## 5. API Layer (Backend)

- [ ] 5.1 Create `AssessmentTeamController` with endpoints: POST /api/v1/assessment-teams, POST /api/v1/assessment-teams/preview, POST /api/v1/assessment-teams/join, GET /api/v1/assessment-teams/my-team, POST /api/v1/assessment-teams/leave, POST /api/v1/assessment-teams/transfer, DELETE /api/v1/assessment-teams/{id}
- [ ] 5.2 Create `AssessmentTeamDTO`, `CreateTeamRequestDTO`, `JoinTeamRequestDTO`, `PreviewTeamRequestDTO`, `TeamMemberDTO` (reuse UserInfo fields)
- [ ] 5.3 Create request/response converters for team DTOs
- [ ] 5.4 Add `@RequiresPermission` annotations to all team endpoints with proper `value` ensuring global uniqueness
- [ ] 5.5 Modify `AdminAssessmentTimeController` create/update endpoints to accept `allowTeam` field
- [ ] 5.6 Modify `AssessmentTimeDTO` to include `allowTeam` field
- [ ] 5.7 Modify `AssessmentAnswerController` if needed for team-related answer queries

## 6. Frontend - Admin

- [ ] 6.1 Update `AssessmentTimeDrawer.tsx` to add "允许组队" switch
- [ ] 6.2 Update admin assessment time list to display "允许组队" status
- [ ] 6.3 Update assessment time form validation to handle `allowTeam`

## 7. Frontend - User Assessment List

- [ ] 7.1 Update `AssessmentCard` component to show team-enabled indicator
- [ ] 7.1 Verify cross-direction assessments (`direction = null`) display correctly in assessment list

## 8. Frontend - Question List Page

- [ ] 8.1 Add team status query on `/assessment/{timeId}/questions` page (call getMyTeam on load)
- [ ] 8.2 For FILE_UPLOAD questions in team-enabled assessments: show "创建队伍" or "加入队伍" button when not in team
- [ ] 8.3 For FILE_UPLOAD questions in team-enabled assessments: show team name and member count when already in team
- [ ] 8.4 Non-FILE_UPLOAD questions remain unchanged regardless of team settings

## 9. Frontend - Question Detail Page (FILE_UPLOAD)

- [ ] 9.1 Add team info panel showing team name, leader, members with direction badges
- [ ] 9.2 Add invite code display with copy-to-clipboard button (visible to leader)
- [ ] 9.3 Add "加入队伍" modal with invite code input and preview confirmation flow
- [ ] 9.4 Leader view: show file upload component, submit/update buttons
- [ ] 9.5 Member view: show leader-submitted files as read-only list with "no permission" message
- [ ] 9.6 Reuse existing UserInfo display components for member list

## 10. Frontend - Team Management

- [ ] 10.1 Add "退出队伍" button for members (with confirmation)
- [ ] 10.2 Add "转让队长" dialog for leader (select from members)
- [ ] 10.3 Add "解散队伍" button for leader (with confirmation)

## 11. Testing

- [ ] 11.1 Write unit tests for `AssessmentTeamAppServiceImpl` (create, join, leave, transfer, preview)
- [ ] 11.2 Write unit tests for team permission validation in `AssessmentAnswerAppServiceImpl`
- [ ] 11.3 Write unit tests for cross-direction query in `AssessmentTimeAppServiceImpl`
- [ ] 11.4 Write integration tests for `AssessmentTeamController` endpoints
- [ ] 11.5 Write repository tests for `AssessmentTeamRepositoryImpl`
- [ ] 11.6 Run full backend test suite to verify no regressions

## 12. E2E Verification

- [ ] 12.1 Build and deploy backend Docker image
- [ ] 12.2 Create cross-direction assessment with `allowTeam = true`
- [ ] 12.3 Create FILE_UPLOAD question in the assessment
- [ ] 12.4 End-to-end test: User A (CV) creates team, User B (电控) joins via invite code with preview confirmation
- [ ] 12.5 End-to-end test: Leader submits file, member views the file
- [ ] 12.6 End-to-end test: Non-team user cannot access FILE_UPLOAD question in team-enabled assessment
- [ ] 12.7 End-to-end test: Judge scores each team member independently

