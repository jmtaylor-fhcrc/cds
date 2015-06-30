SELECT
DISTINCT arm_visit.visit_type AS visit_tag,
visit.rowid AS visit_row_id,
studygroup.row_id AS study_group_id,
arm_visit.prot,
FALSE AS single_use,
arm_visit.isvaccvis AS is_vaccination,
-- CASE WHEN arm_visit.visit_type = 'Vaccination' THEN TRUE ELSE FALSE END AS is_vaccination,
FROM cds.import_studypartgrouparmvisit AS arm_visit
JOIN (SELECT * FROM cds.studygroup) AS studygroup ON (studygroup.container.name = arm_visit.prot AND studygroup.group_name = arm_visit.study_group)
JOIN (SELECT * FROM study.visit) AS visit ON (visit.Folder.Name = arm_visit.prot AND visit.sequencenummin = CAST(arm_visit.study_day AS DOUBLE))
WHERE arm_visit.visit_type IS NOT NULL

UNION

SELECT
DISTINCT arm_visit.visit_align_tag AS visit_tag,
visit.rowid AS visit_row_id,
studygroup.row_id AS study_group_id,
arm_visit.prot,
TRUE AS single_use,
FALSE AS is_vaccination,
FROM cds.import_studypartgrouparmvisit AS arm_visit
JOIN (SELECT * FROM cds.studygroup) AS studygroup ON (studygroup.container.name = arm_visit.prot AND studygroup.group_name = arm_visit.study_group)
JOIN (SELECT * FROM study.visit) AS visit ON (visit.Folder.Name = arm_visit.prot AND visit.sequencenummin = CAST(arm_visit.study_day AS DOUBLE))
WHERE arm_visit.visit_align_tag IS NOT NULL
