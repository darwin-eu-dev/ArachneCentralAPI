TRUNCATE TABLE study_types CASCADE;

INSERT INTO study_types (name)
VALUES ('Complex')
ON CONFLICT (name) DO NOTHING;

INSERT INTO study_types (name)
VALUES ('Off the shelf')
ON CONFLICT (name) DO NOTHING;

INSERT INTO study_types (name)
VALUES ('Routine repeated')
ON CONFLICT (name) DO NOTHING;

INSERT INTO study_types (name)
VALUES ('Very complex')
ON CONFLICT (name) DO NOTHING;

INSERT INTO study_types (name)
VALUES ('Other')
ON CONFLICT (name) DO NOTHING;
