INSERT INTO public.monitored_parameters_types (id, description, name, type, system_parameter, require_target_object, target_object_name, parent_id, multiplier, unit) VALUES ('6a5dbd45-8b51-457e-93c2-9524a267c038', 'Liczba plików', 'FilesCount(/home/patryk/tmp/tmp_test/test)', 'Int', false, true, 'Ścieżka', '24787bdf-3933-4cc5-81f8-28f579d569e4', 1, '') ON CONFLICT DO NOTHING;
INSERT INTO public.monitored_parameters_types (id, description, name, type, system_parameter, require_target_object, target_object_name, parent_id, multiplier, unit) VALUES ('658f1346-7e19-4f90-a52e-617ad85aa772', 'Liczba plików', 'FilesCount(asdasd)', 'Int', false, true, 'Ścieżka', '24787bdf-3933-4cc5-81f8-28f579d569e4', 1, '') ON CONFLICT DO NOTHING;
INSERT INTO public.monitored_parameters_types (id, description, name, type, system_parameter, require_target_object, target_object_name, parent_id, multiplier, unit) VALUES ('24787bdf-3933-4cc5-81f8-28f579d569e4', 'Liczba plików', 'FilesCount', 'Int', false, true, 'Ścieżka', null, 1, '') ON CONFLICT DO NOTHING;
INSERT INTO public.monitored_parameters_types (id, description, name, type, system_parameter, require_target_object, target_object_name, parent_id, multiplier, unit) VALUES ('2b03aa81-2d84-43da-a544-6b3e4160e264', 'Liczba plików', 'FilesCount(null)', 'Int', false, true, 'Ścieżka', '24787bdf-3933-4cc5-81f8-28f579d569e4', 1, '') ON CONFLICT DO NOTHING;
INSERT INTO public.monitored_parameters_types (id, description, name, type, system_parameter, require_target_object, target_object_name, parent_id, multiplier, unit) VALUES ('73aec467-e83f-4ba9-8fa6-002dcad062b9', 'Użycie procesora przez agenta', 'AgentCPUUsage', 'Double', true, false, null, null, 1, '%') ON CONFLICT DO NOTHING;
INSERT INTO public.monitored_parameters_types (id, description, name, type, system_parameter, require_target_object, target_object_name, parent_id, multiplier, unit) VALUES ('dea8310d-036c-4122-b43c-19896254d3ce', 'Użycie CPU', 'CPUUsage', 'Double', true, false, null, null, 1, '%') ON CONFLICT DO NOTHING;
INSERT INTO public.monitored_parameters_types (id, description, name, type, system_parameter, require_target_object, target_object_name, parent_id, multiplier, unit) VALUES ('aa094754-6e2a-4502-bd6d-8ec08ba1fd30', 'Wolna pamięć', 'FreePhysicalMemory', 'Double', true, false, null, null, 0.000001, 'mb') ON CONFLICT DO NOTHING;
INSERT INTO public.monitored_parameters_types (id, description, name, type, system_parameter, require_target_object, target_object_name, parent_id, multiplier, unit) VALUES ('eb541327-8bbe-411c-a882-7a603caf8bc7', 'Wolna pamięć wymiany', 'FreeSwapSpaceSize', 'Double', true, false, null, null, 0.000001, 'mb') ON CONFLICT DO NOTHING;
INSERT INTO public.monitored_parameters_types (id, description, name, type, system_parameter, require_target_object, target_object_name, parent_id, multiplier, unit) VALUES ('8c99fbfb-cd42-4fb5-8e3a-80f6653aa046', 'Użyta pamięć', 'UsedMemorySize', 'Double', true, false, null, null, 0.000001, 'mb') ON CONFLICT DO NOTHING;
INSERT INTO public.monitored_parameters_types (id, description, name, type, system_parameter, require_target_object, target_object_name, parent_id, multiplier, unit) VALUES ('db0af994-727c-4883-878f-d19f7bbbde7d', 'Użyta pamięć wymainy', 'UsedSwapSpaceSize', 'Double', true, false, null, null, 0.000001, 'mb') ON CONFLICT DO NOTHING;


INSERT INTO public.user_roles_types (id, name) VALUES (1, 'ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO public.user_roles_types (id, name) VALUES (3, 'ROLE_ADMINISTRATOR') ON CONFLICT DO NOTHING;
INSERT INTO public.user_roles_types (id, name) VALUES (2, 'ROLE_OPERATOR') ON CONFLICT DO NOTHING;

INSERT INTO public.system_settings (key, value) VALUES ('app_webservice_workers_count', '10') ON CONFLICT DO NOTHING;
INSERT INTO public.system_settings (key, value) VALUES ('app_alerts_checking_interval', '10000') ON CONFLICT DO NOTHING;
INSERT INTO public.system_settings (key, value) VALUES ('app_smtp_server', 'in-v3.mailjet.com') ON CONFLICT DO NOTHING;
INSERT INTO public.system_settings (key, value) VALUES ('app_smtp_username', 'edae4a71a7896acd47833afe861247c1') ON CONFLICT DO NOTHING;
INSERT INTO public.system_settings (key, value) VALUES ('app_smtp_password', '5cb07d8d823f0aeef38a441a9ecddb0b') ON CONFLICT DO NOTHING;
INSERT INTO public.system_settings (key, value) VALUES ('app_smtp_port', '587') ON CONFLICT DO NOTHING;
INSERT INTO public.system_settings (key, value) VALUES ('app_smtp_from_address', 'pat35@op.pl') ON CONFLICT DO NOTHING;
INSERT INTO public.system_settings (key, value) VALUES ('app_charts_max_values_count', '1000') ON CONFLICT DO NOTHING;
INSERT INTO public.system_settings (key, value) VALUES ('app_smtp_mails_footer_name', 'Network Services Monitor Team') ON CONFLICT DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_monitoring_timestamp ON collected_parameters_values(timestamp);
CREATE INDEX IF NOT EXISTS idx_monitoring_timestamp ON collected_logs(timestamp);