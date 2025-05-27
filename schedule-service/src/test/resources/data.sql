INSERT INTO gyms (name, city, address, phone_number, opening_hour, closing_hour)
VALUES ('Jim', 'Warszawa', 'grzybowa 13', '123456789', '08:00:00', '22:00:00');

INSERT INTO group_class_definition (name, description)
VALUES ( 'Yoga', 'Be there or be square');

INSERT INTO gym_group_class_offering (gym_id, group_class_definition_id)
VALUES (1, 1);

INSERT INTO recurring_group_class_schedule (
    gym_group_class_offering_id,
    trainer_id,
    day_of_week,
    start_time,
    end_time,
    start_date,
    end_date,
    frequency,
    capacity
) VALUES (
             1,
             2,
             'MONDAY',
             '08:00:00',
             '09:00:00',
             '2024-06-03',
             '2024-06-24',
             'WEEKLY',
             12
         );