CREATE TABLE IF NOT EXISTS obec (
    kod INT PRIMARY KEY,
    nazev VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS cast_obce (
    kod INT PRIMARY KEY,
    nazev VARCHAR(255) NOT NULL,
    obec_kod INT,
    FOREIGN KEY (obec_kod) REFERENCES obec(kod)
);
