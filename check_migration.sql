-- Check if zip_code column exists
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'users' 
ORDER BY ordinal_position;

-- Check migration history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
