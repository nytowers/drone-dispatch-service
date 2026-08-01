INSERT INTO drones (serial_number, model, weight_limit, battery_capacity, state) VALUES
  ('DRN-0001-LW', 'LIGHTWEIGHT',   200, 100, 'IDLE'),
  ('DRN-0002-LW', 'LIGHTWEIGHT',   250,  20, 'IDLE'),
  ('DRN-0003-MW', 'MIDDLEWEIGHT',  500,  85, 'IDLE'),
  ('DRN-0004-MW', 'MIDDLEWEIGHT',  450,  60, 'LOADED'),
  ('DRN-0005-CW', 'CRUISERWEIGHT', 750,  24, 'IDLE'),
  ('DRN-0006-HW', 'HEAVYWEIGHT',  1000, 100, 'IDLE'),
  ('DRN-0007-LW', 'LIGHTWEIGHT',   250,  70, 'IDLE'),
  ('DRN-0008-MW', 'MIDDLEWEIGHT',  400,  15, 'IDLE'),
  ('DRN-0009-CW', 'CRUISERWEIGHT', 600,  90, 'IDLE'),
  ('DRN-0010-HW', 'HEAVYWEIGHT',   850,  35, 'IDLE');

INSERT INTO medications (name, weight, code, image, drone_id) VALUES
  ('Paracetamol_500', 150, 'PARA_500', NULL, (SELECT id FROM drones WHERE serial_number = 'DRN-0004-MW')),
  ('Amoxicillin-250', 200, 'AMOX_250', NULL, (SELECT id FROM drones WHERE serial_number = 'DRN-0004-MW'));