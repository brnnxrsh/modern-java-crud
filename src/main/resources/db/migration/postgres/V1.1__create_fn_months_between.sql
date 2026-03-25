CREATE OR REPLACE FUNCTION fn_months_between(d1 DATE, d2 DATE)
RETURNS INT AS $$
BEGIN
    RETURN COALESCE(
        (EXTRACT(YEAR FROM age(d2, d1)) * 12 + 
         EXTRACT(MONTH FROM age(d2, d1)))::INT, 
        0
    );
END;
$$ LANGUAGE plpgsql IMMUTABLE;
