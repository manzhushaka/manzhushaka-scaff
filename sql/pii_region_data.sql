-- ============================================================================
-- 执行前请确保当前数据库默认字符集为 utf8mb4，并使用 utf8mb4 客户端导入，
-- 避免行政区划中文名称出现乱码。
-- ============================================================================
SET NAMES utf8mb4;

-- ============================================================================
-- 海南行政区划预置（写入 sys_dept，dept_type=region）
-- 执行顺序：必须先执行 sql/manzhushaka_db_init.sql 完成 sys_dept 扩展
-- ============================================================================

-- 精简版覆盖：海南省 + 19 市县 + 海口/三亚/儋州/三沙主要区划，共 33 行。
-- 完整区/镇清单按民政部 2025 年行政区划数据在部署前补全。
delete from sys_dept where dept_type = 'region' and dept_id between 200 and 399;

-- 海南省级 dept
insert into sys_dept (
    dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email,
    status, del_flag, create_by, create_time, update_by, update_time,
    dept_type, region_code, region_level
) values (
    200, 100, '0,100', '海南省', 0, null, null, null,
    '0', '0', 'admin', now(), '', null,
    'region', '460000', 1
);

-- 市县 dept（level=2，19 个）
insert into sys_dept (
    dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email,
    status, del_flag, create_by, create_time, update_by, update_time,
    dept_type, region_code, region_level
) values
(201, 200, '0,100,200', '海口市', 1, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460100', 2),
(202, 200, '0,100,200', '三亚市', 2, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460200', 2),
(203, 200, '0,100,200', '三沙市', 3, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460300', 2),
(204, 200, '0,100,200', '儋州市', 4, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460400', 2),
(205, 200, '0,100,200', '五指山市', 5, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469001', 2),
(206, 200, '0,100,200', '琼海市', 6, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469002', 2),
(207, 200, '0,100,200', '万宁市', 7, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469006', 2),
(219, 200, '0,100,200', '文昌市', 8, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469005', 2),
(208, 200, '0,100,200', '东方市', 9, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469007', 2),
(209, 200, '0,100,200', '定安县', 10, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469021', 2),
(210, 200, '0,100,200', '屯昌县', 11, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469022', 2),
(211, 200, '0,100,200', '澄迈县', 12, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469023', 2),
(212, 200, '0,100,200', '临高县', 13, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469024', 2),
(213, 200, '0,100,200', '白沙黎族自治县', 14, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469025', 2),
(214, 200, '0,100,200', '昌江黎族自治县', 15, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469026', 2),
(215, 200, '0,100,200', '乐东黎族自治县', 16, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469027', 2),
(216, 200, '0,100,200', '陵水黎族自治县', 17, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469028', 2),
(217, 200, '0,100,200', '保亭黎族苗族自治县', 18, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469029', 2),
(218, 200, '0,100,200', '琼中黎族苗族自治县', 19, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '469030', 2);

-- 海口市区/镇（level=3）
insert into sys_dept (
    dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email,
    status, del_flag, create_by, create_time, update_by, update_time,
    dept_type, region_code, region_level
) values
(301, 201, '0,100,200,201', '秀英区', 1, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460105', 3),
(302, 201, '0,100,200,201', '龙华区', 2, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460106', 3),
(303, 201, '0,100,200,201', '琼山区', 3, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460107', 3),
(304, 201, '0,100,200,201', '美兰区', 4, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460108', 3);

-- 三亚市区（level=3）
insert into sys_dept (
    dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email,
    status, del_flag, create_by, create_time, update_by, update_time,
    dept_type, region_code, region_level
) values
(311, 202, '0,100,200,202', '吉阳区', 1, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460203', 3),
(312, 202, '0,100,200,202', '天涯区', 2, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460204', 3),
(313, 202, '0,100,200,202', '海棠区', 3, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460202', 3),
(314, 202, '0,100,200,202', '崖州区', 4, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460205', 3);

-- 儋州市辖区/镇（level=3）— 简化为那大镇 + 滨海新区
insert into sys_dept (
    dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email,
    status, del_flag, create_by, create_time, update_by, update_time,
    dept_type, region_code, region_level
) values
(321, 204, '0,100,200,204', '那大镇', 1, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460403', 3),
(322, 204, '0,100,200,204', '滨海新区', 2, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460404', 3);

-- 三沙市（西沙群岛等）— 无下辖区/镇，level=3 占位
insert into sys_dept (
    dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email,
    status, del_flag, create_by, create_time, update_by, update_time,
    dept_type, region_code, region_level
) values
(331, 203, '0,100,200,203', '西沙群岛', 1, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460301', 3),
(332, 203, '0,100,200,203', '南沙群岛', 2, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460302', 3),
(333, 203, '0,100,200,203', '中沙群岛', 3, null, null, null, '0', '0', 'admin', now(), '', null, 'region', '460303', 3);
