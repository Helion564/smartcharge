-- Seed data for Indian EV Charging Stations (Tata Power, Statiq, ChargeZone, Ather Grid, EESL)

-- 1. Tata Power - Bandra Kurla Complex (BKC), Mumbai
INSERT INTO charging_station (id, ocm_id, name, address_line1, address_line2, city, state, pincode, operator_id, operator_name, latitude, longitude, status, amenities, operating_hours, is_24_hours, contact_phone, contact_email, website, data_source, created_at, updated_at)
VALUES ('a1111111-1111-1111-1111-111111111101', 'OCM-IN-001', 'Tata Power EZ Charge - BKC Hub', 'G Block, Bandra Kurla Complex', 'Near MMRDA Grounds', 'Mumbai', 'Maharashtra', '400051', 'tata-power-op', 'Tata Power', 19.0657, 72.8687, 'ACTIVE', '["WIFI","CAFE","RESTROOM","PARKING"]', '24x7', true, '+91-22-66658282', 'customercare@tatapower.com', 'https://www.tatapower.com', 'BEE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO connector (id, station_id, connector_code, connector_type, power_level, power_kw, status, price_per_kwh, price_per_minute, is_available, last_status_update)
VALUES 
('c1111111-1111-1111-1111-111111111101', 'a1111111-1111-1111-1111-111111111101', 'TP-BKC-01', 'CCS', 'DC_FAST', 60.0, 'AVAILABLE', 16.50, 0.0, true, NOW()),
('c1111111-1111-1111-1111-111111111102', 'a1111111-1111-1111-1111-111111111101', 'TP-BKC-02', 'CHADEMO', 'DC_FAST', 50.0, 'AVAILABLE', 16.50, 0.0, true, NOW()),
('c1111111-1111-1111-1111-111111111103', 'a1111111-1111-1111-1111-111111111101', 'TP-BKC-03', 'TYPE2', 'LEVEL_2', 22.0, 'AVAILABLE', 12.00, 0.0, true, NOW())
ON CONFLICT (id) DO NOTHING;

-- 2. Statiq EV Charging - Cyber City, Gurugram (Delhi NCR)
INSERT INTO charging_station (id, ocm_id, name, address_line1, address_line2, city, state, pincode, operator_id, operator_name, latitude, longitude, status, amenities, operating_hours, is_24_hours, contact_phone, contact_email, website, data_source, created_at, updated_at)
VALUES ('a1111111-1111-1111-1111-111111111102', 'OCM-IN-002', 'Statiq Charging Hub - DLF Cyber City', 'Building 10, DLF CyberHub', 'DLF Phase 2', 'Gurugram', 'Haryana', '122002', 'statiq-op', 'Statiq', 28.4950, 77.0895, 'ACTIVE', '["WIFI","CAFE","SHOPPING","ATM"]', '24x7', true, '+91-124-4567890', 'support@statiq.in', 'https://www.statiq.in', 'BEE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO connector (id, station_id, connector_code, connector_type, power_level, power_kw, status, price_per_kwh, price_per_minute, is_available, last_status_update)
VALUES 
('c1111111-1111-1111-1111-111111111104', 'a1111111-1111-1111-1111-111111111102', 'STAT-CYBER-01', 'CCS', 'ULTRA_FAST', 120.0, 'AVAILABLE', 18.00, 0.0, true, NOW()),
('c1111111-1111-1111-1111-111111111105', 'a1111111-1111-1111-1111-111111111102', 'STAT-CYBER-02', 'CCS', 'DC_FAST', 60.0, 'AVAILABLE', 16.00, 0.0, true, NOW())
ON CONFLICT (id) DO NOTHING;

-- 3. ChargeZone - Indiranagar, Bengaluru
INSERT INTO charging_station (id, ocm_id, name, address_line1, address_line2, city, state, pincode, operator_id, operator_name, latitude, longitude, status, amenities, operating_hours, is_24_hours, contact_phone, contact_email, website, data_source, created_at, updated_at)
VALUES ('a1111111-1111-1111-1111-111111111103', 'OCM-IN-003', 'ChargeZone Fast Charging - Indiranagar', '100 Feet Road, HAL 2nd Stage', 'Opposite Toit Brewery', 'Bengaluru', 'Karnataka', '560038', 'chargezone-op', 'ChargeZone', 12.9716, 77.6412, 'ACTIVE', '["RESTAURANT","PARKING"]', '06:00 - 23:00', false, '+91-80-87654321', 'info@chargezone.com', 'https://www.chargezone.com', 'BEE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO connector (id, station_id, connector_code, connector_type, power_level, power_kw, status, price_per_kwh, price_per_minute, is_available, last_status_update)
VALUES 
('c1111111-1111-1111-1111-111111111106', 'a1111111-1111-1111-1111-111111111103', 'CZ-BLR-01', 'CCS', 'DC_FAST', 50.0, 'AVAILABLE', 15.50, 0.0, true, NOW()),
('c1111111-1111-1111-1111-111111111107', 'a1111111-1111-1111-1111-111111111103', 'CZ-BLR-02', 'TYPE2', 'LEVEL_2', 22.0, 'AVAILABLE', 11.00, 0.0, true, NOW())
ON CONFLICT (id) DO NOTHING;

-- 4. Ather Grid - Koramangala, Bengaluru
INSERT INTO charging_station (id, ocm_id, name, address_line1, address_line2, city, state, pincode, operator_id, operator_name, latitude, longitude, status, amenities, operating_hours, is_24_hours, contact_phone, contact_email, website, data_source, created_at, updated_at)
VALUES ('a1111111-1111-1111-1111-111111111104', 'OCM-IN-004', 'Ather Grid Point - Koramangala 5th Block', '80 Feet Road, 5th Block', 'Near Sony World Signal', 'Bengaluru', 'Karnataka', '560095', 'ather-op', 'Ather Grid', 12.9352, 77.6245, 'ACTIVE', '["CAFE","WIFI"]', '24x7', true, '+91-80-66465758', 'grid@atherenergy.com', 'https://www.atherenergy.com', 'OCM', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO connector (id, station_id, connector_code, connector_type, power_level, power_kw, status, price_per_kwh, price_per_minute, is_available, last_status_update)
VALUES 
('c1111111-1111-1111-1111-111111111108', 'a1111111-1111-1111-1111-111111111104', 'ATH-KOR-01', 'BHARAT_AC001', 'LEVEL_1', 3.3, 'AVAILABLE', 9.50, 0.0, true, NOW()),
('c1111111-1111-1111-1111-111111111109', 'a1111111-1111-1111-1111-111111111104', 'ATH-KOR-02', 'BHARAT_DC001', 'DC_FAST', 15.0, 'AVAILABLE', 12.00, 0.0, true, NOW())
ON CONFLICT (id) DO NOTHING;

-- 5. EESL Public Station - Connaught Place, New Delhi
INSERT INTO charging_station (id, ocm_id, name, address_line1, address_line2, city, state, pincode, operator_id, operator_name, latitude, longitude, status, amenities, operating_hours, is_24_hours, contact_phone, contact_email, website, data_source, created_at, updated_at)
VALUES ('a1111111-1111-1111-1111-111111111105', 'OCM-IN-005', 'EESL Public EV Station - CP Central Park', 'Inner Circle, Connaught Place', 'Near Gate 1 Metro', 'New Delhi', 'Delhi', '110001', 'eesl-op', 'EESL', 28.6315, 77.2167, 'ACTIVE', '["RESTROOM","PARKING","METRO_CONNECTIVITY"]', '24x7', true, '+91-11-23456789', 'ev@eesl.co.in', 'https://www.eeslindia.org', 'BEE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO connector (id, station_id, connector_code, connector_type, power_level, power_kw, status, price_per_kwh, price_per_minute, is_available, last_status_update)
VALUES 
('c1111111-1111-1111-1111-111111111110', 'a1111111-1111-1111-1111-111111111105', 'EESL-CP-01', 'CCS', 'DC_FAST', 50.0, 'AVAILABLE', 14.00, 0.0, true, NOW()),
('c1111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111105', 'EESL-CP-02', 'BHARAT_DC001', 'DC_FAST', 15.0, 'AVAILABLE', 10.00, 0.0, true, NOW())
ON CONFLICT (id) DO NOTHING;

-- 6. Tata Power - HITEC City, Hyderabad
INSERT INTO charging_station (id, ocm_id, name, address_line1, address_line2, city, state, pincode, operator_id, operator_name, latitude, longitude, status, amenities, operating_hours, is_24_hours, contact_phone, contact_email, website, data_source, created_at, updated_at)
VALUES ('a1111111-1111-1111-1111-111111111106', 'OCM-IN-006', 'Tata Power EV Charging Station - Inorbit Mall', 'APIIC Software Layout, Mindspace', 'HITEC City, Madhapur', 'Hyderabad', 'Telangana', '500081', 'tata-power-op', 'Tata Power', 17.4348, 78.3868, 'ACTIVE', '["MALL","WIFI","CAFE","PARKING"]', '10:00 - 23:00', false, '+91-40-66778899', 'evcustomercare@tatapower.com', 'https://www.tatapower.com', 'BEE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO connector (id, station_id, connector_code, connector_type, power_level, power_kw, status, price_per_kwh, price_per_minute, is_available, last_status_update)
VALUES 
('c1111111-1111-1111-1111-111111111112', 'a1111111-1111-1111-1111-111111111106', 'TP-HYD-01', 'CCS', 'DC_FAST', 60.0, 'AVAILABLE', 16.00, 0.0, true, NOW()),
('c1111111-1111-1111-1111-111111111113', 'a1111111-1111-1111-1111-111111111106', 'TP-HYD-02', 'TYPE2', 'LEVEL_2', 22.0, 'AVAILABLE', 11.50, 0.0, true, NOW())
ON CONFLICT (id) DO NOTHING;

-- 7. Charge+Zone - Express Avenue, Chennai
INSERT INTO charging_station (id, ocm_id, name, address_line1, address_line2, city, state, pincode, operator_id, operator_name, latitude, longitude, status, amenities, operating_hours, is_24_hours, contact_phone, contact_email, website, data_source, created_at, updated_at)
VALUES ('a1111111-1111-1111-1111-111111111107', 'OCM-IN-007', 'ChargeZone Hub - Express Avenue Mall', 'Club House Road, Royapettah', 'Whites Road', 'Chennai', 'Tamil Nadu', '600002', 'chargezone-op', 'ChargeZone', 13.0587, 80.2642, 'ACTIVE', '["MALL","FOOD_COURT","CINEMA"]', '10:00 - 22:30', false, '+91-44-28464646', 'support@chargezone.com', 'https://www.chargezone.com', 'BEE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO connector (id, station_id, connector_code, connector_type, power_level, power_kw, status, price_per_kwh, price_per_minute, is_available, last_status_update)
VALUES 
('c1111111-1111-1111-1111-111111111114', 'a1111111-1111-1111-1111-111111111107', 'CZ-MAA-01', 'CCS', 'DC_FAST', 50.0, 'AVAILABLE', 15.00, 0.0, true, NOW()),
('c1111111-1111-1111-1111-111111111115', 'a1111111-1111-1111-1111-111111111107', 'CZ-MAA-02', 'CHADEMO', 'DC_FAST', 50.0, 'AVAILABLE', 15.00, 0.0, true, NOW())
ON CONFLICT (id) DO NOTHING;

-- 8. Statiq - Baner, Pune
INSERT INTO charging_station (id, ocm_id, name, address_line1, address_line2, city, state, pincode, operator_id, operator_name, latitude, longitude, status, amenities, operating_hours, is_24_hours, contact_phone, contact_email, website, data_source, created_at, updated_at)
VALUES ('a1111111-1111-1111-1111-111111111108', 'OCM-IN-008', 'Statiq Fast Charger - Baner Road', 'Near Balewadi High Street', 'Baner', 'Pune', 'Maharashtra', '411045', 'statiq-op', 'Statiq', 18.5590, 73.7868, 'ACTIVE', '["CAFE","RESTROOM"]', '24x7', true, '+91-20-41234567', 'pune@statiq.in', 'https://www.statiq.in', 'OCM', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO connector (id, station_id, connector_code, connector_type, power_level, power_kw, status, price_per_kwh, price_per_minute, is_available, last_status_update)
VALUES 
('c1111111-1111-1111-1111-111111111116', 'a1111111-1111-1111-1111-111111111108', 'STAT-PUN-01', 'CCS', 'ULTRA_FAST', 120.0, 'AVAILABLE', 17.50, 0.0, true, NOW()),
('c1111111-1111-1111-1111-111111111117', 'a1111111-1111-1111-1111-111111111108', 'STAT-PUN-02', 'TYPE2', 'LEVEL_2', 22.0, 'AVAILABLE', 12.00, 0.0, true, NOW())
ON CONFLICT (id) DO NOTHING;

-- 9. Tata Power - SG Highway, Ahmedabad
INSERT INTO charging_station (id, ocm_id, name, address_line1, address_line2, city, state, pincode, operator_id, operator_name, latitude, longitude, status, amenities, operating_hours, is_24_hours, contact_phone, contact_email, website, data_source, created_at, updated_at)
VALUES ('a1111111-1111-1111-1111-111111111109', 'OCM-IN-009', 'Tata Power EV Hub - Iscon Mega Mall', 'Near Iscon Cross Road', 'S.G. Highway', 'Ahmedabad', 'Gujarat', '380054', 'tata-power-op', 'Tata Power', 23.0298, 72.5065, 'ACTIVE', '["SHOPPING","FOOD_COURT","PARKING"]', '24x7', true, '+91-79-40012345', 'ahmedabad@tatapower.com', 'https://www.tatapower.com', 'BEE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO connector (id, station_id, connector_code, connector_type, power_level, power_kw, status, price_per_kwh, price_per_minute, is_available, last_status_update)
VALUES 
('c1111111-1111-1111-1111-111111111118', 'a1111111-1111-1111-1111-111111111109', 'TP-AMD-01', 'CCS', 'DC_FAST', 60.0, 'AVAILABLE', 15.00, 0.0, true, NOW()),
('c1111111-1111-1111-1111-111111111119', 'a1111111-1111-1111-1111-111111111109', 'TP-AMD-02', 'GB_T', 'DC_FAST', 50.0, 'AVAILABLE', 14.00, 0.0, true, NOW())
ON CONFLICT (id) DO NOTHING;

-- 10. Magenta ChargeGrid - Vashi, Navi Mumbai
INSERT INTO charging_station (id, ocm_id, name, address_line1, address_line2, city, state, pincode, operator_id, operator_name, latitude, longitude, status, amenities, operating_hours, is_24_hours, contact_phone, contact_email, website, data_source, created_at, updated_at)
VALUES ('a1111111-1111-1111-1111-111111111110', 'OCM-IN-010', 'Magenta ChargeGrid - Inorbit Mall Vashi', 'Sector 30A, Vashi', 'Near Vashi Railway Station', 'Navi Mumbai', 'Maharashtra', '400705', 'magenta-op', 'Magenta Mobility', 19.0664, 72.9984, 'ACTIVE', '["MALL","CAFE","PARKING"]', '24x7', true, '+91-22-27812345', 'ev@magentagroup.com', 'https://www.magentamobility.com', 'BEE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO connector (id, station_id, connector_code, connector_type, power_level, power_kw, status, price_per_kwh, price_per_minute, is_available, last_status_update)
VALUES 
('c1111111-1111-1111-1111-111111111120', 'a1111111-1111-1111-1111-111111111110', 'MAG-VASH-01', 'CCS', 'DC_FAST', 50.0, 'AVAILABLE', 15.50, 0.0, true, NOW()),
('c1111111-1111-1111-1111-111111111121', 'a1111111-1111-1111-1111-111111111110', 'MAG-VASH-02', 'TYPE2', 'LEVEL_2', 11.0, 'AVAILABLE', 10.50, 0.0, true, NOW())
ON CONFLICT (id) DO NOTHING;

