INSERT INTO demo_patch_table (id, patch_name)
SELECT 1, 'import_export'
WHERE NOT EXISTS (
  SELECT 1
  FROM demo_patch_table
  WHERE id = 1
);
