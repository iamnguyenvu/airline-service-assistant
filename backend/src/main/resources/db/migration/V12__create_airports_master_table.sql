-- V12: Create airports master data table
-- This replaces hardcoded airport lists in queries

CREATE TABLE airports (
    iata_code VARCHAR(3) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'Vietnam',
    airport_type VARCHAR(20) NOT NULL DEFAULT 'DOMESTIC' CHECK (airport_type IN ('DOMESTIC', 'INTERNATIONAL')),
    active BOOLEAN DEFAULT true,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    timezone VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert Vietnam domestic airports
INSERT INTO airports (iata_code, name, city, country, airport_type) VALUES
('SGN', 'Tan Son Nhat International Airport', 'Ho Chi Minh City', 'Vietnam', 'DOMESTIC'),
('HAN', 'Noi Bai International Airport', 'Hanoi', 'Vietnam', 'DOMESTIC'),
('DAD', 'Da Nang International Airport', 'Da Nang', 'Vietnam', 'DOMESTIC'),
('CXR', 'Cam Ranh International Airport', 'Nha Trang', 'Vietnam', 'DOMESTIC'),
('HPH', 'Cat Bi International Airport', 'Hai Phong', 'Vietnam', 'DOMESTIC'),
('PQC', 'Phu Quoc International Airport', 'Phu Quoc', 'Vietnam', 'DOMESTIC'),
('VCA', 'Can Tho International Airport', 'Can Tho', 'Vietnam', 'DOMESTIC'),
('DLI', 'Lien Khuong Airport', 'Da Lat', 'Vietnam', 'DOMESTIC'),
('UIH', 'Phu Cat Airport', 'Quy Nhon', 'Vietnam', 'DOMESTIC'),
('VCL', 'Chu Lai International Airport', 'Tam Ky', 'Vietnam', 'DOMESTIC'),
('VII', 'Vinh International Airport', 'Vinh', 'Vietnam', 'DOMESTIC'),
('VDO', 'Van Don International Airport', 'Quang Ninh', 'Vietnam', 'DOMESTIC'),
('VCS', 'Con Dao Airport', 'Con Dao', 'Vietnam', 'DOMESTIC'),
('VKG', 'Rach Gia Airport', 'Rach Gia', 'Vietnam', 'DOMESTIC'),
('BMV', 'Buon Ma Thuot Airport', 'Buon Ma Thuot', 'Vietnam', 'DOMESTIC'),
('THD', 'Tho Xuan Airport', 'Thanh Hoa', 'Vietnam', 'DOMESTIC'),
('TBB', 'Dong Tac Airport', 'Tuy Hoa', 'Vietnam', 'DOMESTIC'),
('VDH', 'Dong Hoi Airport', 'Dong Hoi', 'Vietnam', 'DOMESTIC'),
('HUI', 'Phu Bai International Airport', 'Hue', 'Vietnam', 'DOMESTIC');

-- Common international destination airports
INSERT INTO airports (iata_code, name, city, country, airport_type) VALUES
('SIN', 'Changi Airport', 'Singapore', 'Singapore', 'INTERNATIONAL'),
('BKK', 'Suvarnabhumi Airport', 'Bangkok', 'Thailand', 'INTERNATIONAL'),
('KUL', 'Kuala Lumpur International', 'Kuala Lumpur', 'Malaysia', 'INTERNATIONAL'),
('ICN', 'Incheon International Airport', 'Seoul', 'South Korea', 'INTERNATIONAL'),
('NRT', 'Narita International Airport', 'Tokyo', 'Japan', 'INTERNATIONAL'),
('HKG', 'Hong Kong International Airport', 'Hong Kong', 'Hong Kong', 'INTERNATIONAL'),
('TPE', 'Taiwan Taoyuan International', 'Taipei', 'Taiwan', 'INTERNATIONAL'),
('PVG', 'Shanghai Pudong International', 'Shanghai', 'China', 'INTERNATIONAL'),
('SYD', 'Sydney Airport', 'Sydney', 'Australia', 'INTERNATIONAL'),
('MEL', 'Melbourne Airport', 'Melbourne', 'Australia', 'INTERNATIONAL');

-- Create indexes
CREATE INDEX idx_airports_type ON airports(airport_type);
CREATE INDEX idx_airports_country ON airports(country);
CREATE INDEX idx_airports_active ON airports(active);
