## 1. Type Definitions

- [x] 1.1 Create `src/apis/schema/direction.dto.ts`
- [x] 1.2 Define `LearningStepDTO` interface
- [x] 1.3 Define `DirectionLearningPathDTO` interface

## 2. API Service Layer

- [x] 2.1 Create `src/apis/services/direction.service.ts`
- [x] 2.2 Implement `getLearningPath(slug)` function
- [x] 2.3 Configure `publicClient` for API calls
- [x] 2.4 Add error handling

## 3. Page Component Modification

- [x] 3.1 Update `src/app/(public)/(other)/direction/[slug]/page.tsx`
- [x] 3.2 Import direction service
- [x] 3.3 Add ISR configuration (`revalidate = 3600`)
- [x] 3.4 Implement server-side data fetching
- [x] 3.5 Implement data merging logic (static + dynamic)
- [x] 3.6 Add error handling with fallback to static data

## 4. Testing & Verification

- [ ] 4.1 Test page rendering with valid slug
- [ ] 4.2 Test page rendering with video links
- [ ] 4.3 Test fallback behavior when API fails
- [ ] 4.4 Verify ISR caching works correctly
- [ ] 4.5 Test on mobile viewport
