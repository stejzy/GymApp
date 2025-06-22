Feature: Recurring Group Class Schedule Management

  Scenario: Add a recurring group class schedule successfully
    Given a gym group class offering with id 1 exists
    When I add a recurring group class schedule with the following data:
      | gymGroupClassOfferingId | trainerId | dayOfWeek | startTime | endTime | startDate  | endDate    | frequency | capacity |
      | 1                       | 5         | MONDAY    | 10:00     | 11:00   | TODAY+2DAYS | TODAY+20DAYS | WEEKLY    | 20       |
    Then the recurring group class schedule is saved successfully
    And the returned schedule contains the gymGroupClassOfferingId 1
    And the returned schedule contains the trainerId 5
    And the returned schedule contains dayOfWeek MONDAY
