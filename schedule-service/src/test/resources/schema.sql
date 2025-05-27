CREATE TABLE gyms (
                      gym_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      name VARCHAR(255),
                      city VARCHAR(255),
                      address VARCHAR(255),
                      phone_number VARCHAR(255),
                      opening_hour TIME,
                      closing_hour TIME
);

CREATE TABLE group_class_definition (
                                        group_class_definition_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        name VARCHAR(255),
                                        description VARCHAR(255)
);

CREATE TABLE gym_group_class_offering (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          gym_id BIGINT,
                                          group_class_definition_id BIGINT,
                                          CONSTRAINT fk_gygco_gym FOREIGN KEY (gym_id) REFERENCES gyms(gym_id),
                                          CONSTRAINT fk_gygco_def FOREIGN KEY (group_class_definition_id) REFERENCES group_class_definition(group_class_definition_id)
);

CREATE TABLE recurring_group_class_schedule (
                                                recurring_group_class_schedule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                gym_group_class_offering_id BIGINT,
                                                trainer_id BIGINT,
                                                day_of_week VARCHAR(20),
                                                start_time TIME,
                                                end_time TIME,
                                                start_date DATE,
                                                end_date DATE,
                                                frequency VARCHAR(20),
                                                capacity INT,
                                                CONSTRAINT fk_rgcs_gygco FOREIGN KEY (gym_group_class_offering_id) REFERENCES gym_group_class_offering(id)
);

CREATE TABLE gym_trainer_ids (
                                 gym_id BIGINT NOT NULL,
                                 trainer_id BIGINT,
                                 CONSTRAINT fk_gym FOREIGN KEY (gym_id) REFERENCES gyms(gym_id)
);