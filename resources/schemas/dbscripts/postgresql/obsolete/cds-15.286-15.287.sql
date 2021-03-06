/*
 * Copyright (c) 2016 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Materialize the GridBase query */
DROP TABLE IF EXISTS cds.GridBase;

CREATE TABLE cds.GridBase (
  SubjectId VARCHAR(250),
  Study VARCHAR(250),
  TreatmentSummary VARCHAR(250),
  SubjectVisit VARCHAR(250),

  ParticipantSequenceNum VARCHAR(250),
  SequenceNum NUMERIC(15,4),
  Container ENTITYID,

  CONSTRAINT PK_GridBase_ParticipantSequenceNum PRIMARY KEY (Container, ParticipantSequenceNum)
);

CREATE INDEX IX_GridBase_Study ON cds.GridBase(Study);
CREATE INDEX IX_GridBase_SubjectId ON cds.GridBase(SubjectId);

/* Add Treatment Arm dimension columns to fact table */
ALTER TABLE cds.Facts ADD COLUMN treatment_arm VARCHAR(250);
ALTER TABLE cds.Facts ADD COLUMN study_label VARCHAR(250);
ALTER TABLE cds.Facts ADD COLUMN product_group VARCHAR(250);

/* Alter VisitTagMap to support vaccination, non-vaccination scenarios */
DELETE FROM cds.VisitTagMap;

ALTER TABLE cds.VisitTagMap ADD COLUMN arm_id VARCHAR(250) NOT NULL REFERENCES cds.TreatmentArm (arm_id);
ALTER TABLE cds.VisitTagMap ADD COLUMN detail_label VARCHAR(250);

ALTER TABLE cds.VisitTagMap DROP CONSTRAINT PK_VisitTagMap;
ALTER TABLE cds.VisitTagMap ADD CONSTRAINT PK_VisitTagMap PRIMARY KEY (visit_tag, visit_row_id, study_group_id, arm_id, container);
