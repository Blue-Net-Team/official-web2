## ADDED Requirements

### Requirement: Assessment scope classification

`AssessmentTime` SHALL expose an `AssessmentScope` value object that classifies the scope of an assessment based on its `direction` and `epoch` fields.

#### Scenario: Global final assessment scope
- **WHEN** an assessment has `direction = null` and `epoch = 0`
- **THEN** `AssessmentScope.isGlobalFinal()` SHALL return true
- **AND** `AssessmentScope.isDirectional()` SHALL return false

#### Scenario: Directional assessment scope
- **WHEN** an assessment has a non-null `direction`
- **THEN** `AssessmentScope.isDirectional()` SHALL return true
- **AND** `AssessmentScope.isGlobalFinal()` SHALL return false

#### Scenario: Non-directional assessment scope is neither global final nor directional
- **WHEN** an assessment has `direction = null` and `epoch != 0`
- **THEN** `AssessmentScope.isGlobalFinal()` SHALL return false
- **AND** `AssessmentScope.isDirectional()` SHALL return false

### Requirement: Final round and valid epoch classification

`AssessmentScope` SHALL expose explicit predicates for final round (`epoch = 0`) and valid directional epoch (`epoch > 0`).

#### Scenario: Final round with epoch zero
- **WHEN** an assessment has `epoch = 0`
- **THEN** `AssessmentScope.isFinalRound()` SHALL return true

#### Scenario: Non-final round
- **WHEN** an assessment has `epoch != 0` or `epoch = null`
- **THEN** `AssessmentScope.isFinalRound()` SHALL return false

#### Scenario: Valid directional epoch
- **WHEN** an assessment has `epoch > 0`
- **THEN** `AssessmentScope.isValidDirectionalEpoch()` SHALL return true

#### Scenario: Invalid directional epoch
- **WHEN** an assessment has `epoch <= 0` or `epoch = null`
- **THEN** `AssessmentScope.isValidDirectionalEpoch()` SHALL return false

### Requirement: Grade policy matching

`AssessmentTime` SHALL expose a grade matching policy that treats a null `grade` as a wildcard matching any grade, and a non-null `grade` as an exact match.

#### Scenario: Either grade is a wildcard
- **WHEN** at least one of the two assessments has `grade = null`
- **THEN** the grade policy SHALL match

#### Scenario: Both grades are non-null and equal
- **WHEN** both assessments have the same non-null `grade`
- **THEN** the grade policy SHALL match

#### Scenario: Both grades are non-null and different
- **WHEN** both assessments have non-null but different `grade` values
- **THEN** the grade policy SHALL NOT match

### Requirement: Directional elimination restricts later directional assessments

A prior elimination decision from a directional assessment SHALL restrict a later directional assessment only when both assessments share the same direction and their grades match.

#### Scenario: Same direction and grade with later epoch
- **WHEN** a candidate was eliminated in direction COMPUTER_VISION, grade 2024, epoch 1
- **AND** the target assessment is direction COMPUTER_VISION, grade 2024, epoch 2
- **THEN** the system SHALL report the candidate as eliminated from prior epoch

#### Scenario: Same direction and grade with same epoch
- **WHEN** a candidate was eliminated in direction COMPUTER_VISION, grade 2024, epoch 1
- **AND** the target assessment is direction COMPUTER_VISION, grade 2024, epoch 1
- **THEN** the system SHALL NOT report the candidate as eliminated from prior epoch

#### Scenario: Different direction
- **WHEN** a candidate was eliminated in direction COMPUTER_VISION, grade 2024, epoch 1
- **AND** the target assessment is direction EMBEDDED, grade 2024, epoch 2
- **THEN** the system SHALL NOT report the candidate as eliminated from prior epoch

#### Scenario: Same direction final round
- **WHEN** a candidate was eliminated in direction COMPUTER_VISION, grade 2024, epoch 1
- **AND** the target assessment is direction COMPUTER_VISION, grade 2024, epoch 0
- **THEN** the system SHALL report the candidate as eliminated from prior epoch

### Requirement: Directional elimination restricts global final assessment

A prior elimination decision from a directional assessment with a valid epoch SHALL restrict a global final assessment when the grade policy matches.

#### Scenario: Directional elimination with matching grade restricts global final
- **WHEN** a candidate was eliminated in direction COMPUTER_VISION, grade 2024, epoch 1
- **AND** the target assessment is global final with `direction = null`, `epoch = 0`, grade 2024
- **THEN** the system SHALL report the candidate as eliminated from prior epoch

#### Scenario: Directional elimination with wildcard grade restricts global final
- **WHEN** a candidate was eliminated in direction COMPUTER_VISION, grade null, epoch 1
- **AND** the target assessment is global final with `direction = null`, `epoch = 0`, grade 2024
- **THEN** the system SHALL report the candidate as eliminated from prior epoch

#### Scenario: Directional elimination with non-matching grade does not restrict global final
- **WHEN** a candidate was eliminated in direction COMPUTER_VISION, grade 2024, epoch 1
- **AND** the target assessment is global final with `direction = null`, `epoch = 0`, grade 2023
- **THEN** the system SHALL NOT report the candidate as eliminated from prior epoch

#### Scenario: Directional elimination with invalid epoch does not restrict global final
- **WHEN** a candidate was eliminated in direction COMPUTER_VISION, grade 2024, epoch 0
- **AND** the target assessment is global final with `direction = null`, `epoch = 0`, grade 2024
- **THEN** the system SHALL NOT report the candidate as eliminated from prior epoch

### Requirement: Global or invalid assessment elimination does not restrict other assessments

An elimination decision from a non-directional, non-global-final assessment SHALL NOT restrict any other assessment.

#### Scenario: Global final assessment elimination does not restrict directional assessment
- **WHEN** a candidate was eliminated in a global final assessment
- **AND** the target assessment is a directional assessment
- **THEN** the system SHALL NOT report the candidate as eliminated from prior epoch

### Requirement: Epoch precedence for elimination

`AssessmentTime` SHALL expose an epoch precedence policy that determines whether a prior elimination decision occurred in an earlier valid epoch than the target assessment.

#### Scenario: Valid prior directional epoch before later directional epoch
- **WHEN** the prior epoch is 1 and the target epoch is 2
- **THEN** the prior epoch SHALL be considered earlier

#### Scenario: Valid prior directional epoch before final round
- **WHEN** the prior epoch is 1 and the target epoch is 0
- **THEN** the prior epoch SHALL be considered earlier

#### Scenario: Invalid or non-positive prior epoch is not earlier
- **WHEN** the prior epoch is null, 0, or negative
- **THEN** the prior epoch SHALL NOT be considered earlier regardless of target epoch

#### Scenario: Same epoch is not earlier
- **WHEN** the prior epoch equals the target epoch
- **THEN** the prior epoch SHALL NOT be considered earlier
