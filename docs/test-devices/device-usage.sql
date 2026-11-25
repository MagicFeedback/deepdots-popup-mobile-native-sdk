-- DD-272: device mix of real SDK users, to choose the test device matrix (docs/TEST-DEVICES.md).
-- Read only. Run with bq in our analytics project, for example:
--   bq --project_id=<analytics project> query --use_legacy_sql=false \
--     --parameter=integration::<integration id> --parameter=days:INT64:30 < docs/test-devices/device-usage.sql
-- Only integrations with App Analytics send deepdots_device_model / deepdots_os_version
-- (FeedbackPayload.kt). Users = distinct deepdots_user_id (record id when there is none).
WITH m AS (
  SELECT
    id,
    MAX(IF(metadata_name = 'deepdots_platform', metadata_values, NULL)) AS platform,
    MAX(IF(metadata_name = 'deepdots_device_type', metadata_values, NULL)) AS device_type,
    MAX(IF(metadata_name = 'deepdots_device_model', metadata_values, NULL)) AS model,
    MAX(IF(metadata_name = 'deepdots_os_version', metadata_values, NULL)) AS os,
    MAX(IF(metadata_name = 'deepdots_user_id', metadata_values, NULL)) AS user_id
  FROM `ai.insight_metadata_view`
  WHERE integration = @integration
    AND externalGeneratedAt >= DATETIME_SUB(CURRENT_DATETIME(), INTERVAL @days DAY)
  GROUP BY id
)
SELECT 'os_major' AS dim, platform, REGEXP_EXTRACT(os, r'\d+') AS value, COUNT(DISTINCT COALESCE(user_id, id)) AS users
FROM m GROUP BY 1, 2, 3
UNION ALL
SELECT 'device_type', platform, device_type, COUNT(DISTINCT COALESCE(user_id, id))
FROM m GROUP BY 1, 2, 3
UNION ALL
SELECT 'android_brand', platform, SPLIT(model, ' ')[SAFE_OFFSET(0)], COUNT(DISTINCT COALESCE(user_id, id))
FROM m WHERE platform = 'android' AND model NOT LIKE '%sdk_gphone%' GROUP BY 1, 2, 3
UNION ALL
SELECT 'model', platform, model, COUNT(DISTINCT COALESCE(user_id, id))
FROM m WHERE model NOT LIKE '%sdk_gphone%' GROUP BY 1, 2, 3
ORDER BY dim, platform, users DESC
