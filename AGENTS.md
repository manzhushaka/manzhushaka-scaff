Repository Guidelines

每次回答问题前，都先说问候语：“你好，我是 manzhushaka (≧▽≦)”，并在说完问候语后换行。

## 适用范围与优先级

本文件是本仓库的贡献指南与 AI Agent 协作规范。后端 Java 代码以 P3C《Java 开发手册》（嵩山版）为基线，并结合当前仓库的模块边界执行；前端代码遵循 `ui-admin-arco` 现有 Vue 3 + Arco Design Vue 风格。若本文件与用户明确指令冲突，优先遵循用户指令；若与模块内更具体说明冲突，优先遵循更靠近代码的说明。

P3C 规则按约束力理解为【强制】、【推荐】、【参考】。除非有充分理由并在评审中说明，否则新增和修改代码默认遵守本文件规则。

## 总纲领


| 原则         | 解决的问题                     |
| ------------ | ------------------------------ |
| 编码前思考   | 错误假设、隐藏困惑、缺少权衡   |
| 简洁优先     | 过度复杂、臃肿抽象             |
| 精准修改     | 无关编辑、触碰不应碰的代码     |
| 目标驱动执行 | 通过测试优先、可验证的成功标准 |

## 编码前思考

不要假设，不要隐藏困惑，必须呈现权衡。LLM 容易默默选择一种解释然后直接执行；本仓库要求先把关键推理显性化。

- 明确说明假设：如果不确定，询问而不是猜测。
- 呈现多种解释：当需求存在歧义时，不要默默选择其中一种。
- 适时提出异议：如果存在更简单、更稳妥的方法，要说出来。
- 困惑时停下来：指出不清楚的地方，并要求澄清。
- 改代码前先明确目标、约束、关键假设和验证方式；缺陷修复先定位根因，功能开发先定义预期行为和落点。
- 优先读取真实调用链路，包括入口、Service、Mapper、配置、SQL、前端调用和测试；不要只根据目录名或文件名猜实现。
- 需求存在歧义时，先说明可能解释；默认选择风险最低、改动最小、最符合现有局部模式的方案。

## 简洁优先

用最少的代码解决问题，不做过度推测。

- 不添加要求之外的功能。
- 不为一次性代码创建抽象。
- 不添加未要求的“灵活性”或“可配置性”。
- 不为不可能发生的场景做错误处理。
- 优先复用现有局部模式；不要为了“以后可能会用”提前引入新抽象、新依赖、全局配置或大范围改写。
- 如果 200 行代码可以写成 50 行，应重写并简化。
- 检验标准：资深工程师会觉得这过于复杂吗？如果是，继续简化。

## 精准修改

只碰必须碰的代码，只清理自己造成的混乱。

- 不“顺手改进”相邻代码、注释或格式。
- 不重构没坏的东西。
- 匹配现有风格，即使你更倾向于不同写法。
- 如果注意到无关死代码，可以提出来，但不要删除。
- 删除因本次改动而变得无用的导入、变量、函数和配置。
- 不删除预先存在的死代码，除非用户明确要求。
- 检验标准：每一行修改都应能直接追溯到用户请求。

## 目标驱动执行

先定义成功标准，再循环验证直到达成。把指令式任务转化为可验证目标。


| 用户说法 | 应转化为                            |
| -------- | ----------------------------------- |
| 添加验证 | 为无效输入编写测试，然后让测试通过  |
| 修复 bug | 编写重现 bug 的测试，然后让测试通过 |
| 重构 X   | 确保重构前后测试都能通过            |

多步骤任务应说明简短计划，并为每步绑定验证方式：

1. 步骤一：说明要做什么。验证：说明如何检查。
2. 步骤二：说明要做什么。验证：说明如何检查。
3. 步骤三：说明要做什么。验证：说明如何检查。

强成功标准能支持独立循环执行；“让它工作”这类弱标准需要继续澄清。

## 项目结构与模块边界

- 后端是 Maven 多模块工程：`manzhushaka-admin` 为 Web 启动层、Controller、HTTP DTO/VO 和全局异常入口；`manzhushaka-framework` 为安全、配置、AOP、拦截器等框架能力；`manzhushaka-system` 为系统业务；`manzhushaka-quartz` 为定时任务；`manzhushaka-common` 仅放通用工具、常量、注解和基础能力。
- Java 源码放在 `src/main/java`，资源放在 `src/main/resources`，测试放在 `src/test/java`。
- 前端位于 `ui-admin-arco/src`：页面在 `views`，组件在 `components`，接口在 `api`，状态在 `store`，路由在 `router`，资源和样式在 `assets`。
- SQL 脚本放在 `sql`，文档放在 `doc` 和 `docs`。
- 业务域各自持久化，不新增统一 `db` 模块；`common` 禁止承载业务实体。
- 新功能如果需要调整数据库结构、基础数据或默认配置，必须同步调整 `sql` 目录下的初始化脚本，当前主初始化脚本为 `sql/manzhushaka_db_init.sql`；若后续任务明确引入 `sql/manzhushaka_init.sql`，再同步维护该文件。
- 新增菜单、按钮权限或角色默认授权时，必须同步维护 `sys_menu` 和必要的 `sys_role_menu` 初始化 SQL，不能只改前端路由或后端接口。

## 前端技术现状

- `ui-admin-arco` 的页面和公共组件统一使用 Arco Design Vue，`main.ts` 负责应用入口，`package.json` 不得引入 Element Plus 依赖。
- 所有新增和修改的页面、共享组件统一使用 Arco Design Vue，不得重新引入 Element Plus 依赖、组件 API 或图标。
- 当前仓库不使用统一的 `PageHeader` 和页面说明元数据，业务页面也不要求路由提供页面说明元数据。不要在各页面重新手写统一说明条。
- 调整前端 UI 时默认保留接口字段、权限字符串、路由名称、查询参数和业务流程，不把视觉或组件改动扩大成业务重构。

## 前端视觉设计规范

### 设计基线与主题变量

- `ui-admin-arco` 采用浅色侧栏、白色工作面板和可切换的橙白、紫白主题；切换主题只改变主信号色和对应浅色状态面，不改变壳层明暗关系。页面应安静、紧凑、便于扫描，不使用营销页式大标题、装饰性渐变、光斑或大面积高饱和色。
- 所有业务页面和公共组件必须使用 Arco Design Vue，不得新增 Element Plus 依赖、组件用法或迁移期兼容层。
- 全站颜色、间距、圆角、阴影、控件高度和布局尺寸统一遵循 `ui-admin-arco/src/assets/style` 的主题与样式体系；业务页面禁止重复硬编码主色、背景色和边框色。
- 主色只用于当前导航、主要操作、焦点和关键状态；成功、警告、危险、信息状态分别使用 `--ui-success`、`--ui-warning`、`--ui-danger`、`--ui-info`，不把所有状态都改成橙色。
- 面板和控件圆角默认不超过 `8px`；阴影保持轻量，仅用于需要与画布分层的面板、弹窗和浮层，普通页面区块不做悬浮卡片。

### 应用壳层

- 桌面端侧栏宽度、顶栏高度、页签高度和内容间距分别使用 `--ui-layout-sidebar-width`、`--ui-layout-topbar-height`、`--ui-layout-tags-height`、`--ui-layout-content-padding`，禁止在页面内另写一套壳层尺寸。
- 侧栏保持白色或极浅中性色背景；普通项使用中性灰文字，悬停项使用低对比浅灰背景，当前项使用主题浅色背景、主题色文字和左侧细信号条，不使用深色侧栏或整块高饱和色铺满菜单项。
- 顶栏承载折叠菜单、面包屑、全屏和账号入口；页签栏使用清晰分隔，当前页签通过顶部橙色信号和白色背景识别。
- 页面内容背景使用 `--ui-bg-content`，一级工作面板使用 `--ui-bg-panel`；不得通过每个页面单独改背景制造视觉断层。

### 页面结构

- 标准业务页直接进入工作区，不额外添加营销式 Hero、页面说明条或装饰性概览卡片。
- 查询列表页根容器使用 `app-container ui-list-page`，页面视觉固定为两个面板：筛选区使用独立的 `ui-filter-card`；数据工作区按 `ui-action-bar`、`ui-table-card`、分页的顺序连续拼接。`ui-action-bar` 是数据工作区的表头，必须与表格共用同一外框，不得单独悬在页面背景上、另起圆角卡片，或被额外的 `ui-panel-card`、`a-card` 包裹；分页必须贴合表格底部作为该工作区的页脚。
- Arco 列表表格必须使用 `<a-table>` 包裹 `<template #columns>`，并将所有 `<a-table-column>` 放在该插槽内；不得把列组件直接放入表格默认插槽，否则接口即使返回 `rows` 和 `total`，页面仍会只显示分页而没有表头和数据行。
- 标准列表页遵循 Arco Design Pro 的无外框表格做法：`ui-table-card` 内的 `<a-table>` 必须显式设置 `:bordered="false"`。数据工作区只允许 `ui-action-bar` 与 `ui-table-card` 共同形成一圈外边界；表格自身不得再绘制左右外边框或继承圆角，避免出现双边框和“卡片嵌套卡片”的视觉效果。
- 标准列表数据工作区的 DOM 顺序固定为 `ui-action-bar` 后紧邻 `ui-table-card`；`a-table` 和按需显示的 `pagination` 必须共同放在 `ui-table-card` 内。分页不得作为独立卡片与表格分离，空数据时也必须保留表头和 Arco 空状态。
- 树加列表页面复用 `TreePanel` 和 `tree-sidebar-manage-wrap`；详情页、监控页和资料页可使用 `ui-panel-card`、`ui-detail-card`，但禁止卡片嵌套卡片。
- 登录、注册、锁屏和错误页可以使用独立构图，但必须沿用同一品牌色、字体、控件圆角和明暗关系，不另建平行主题。

### 控件与数据展示

- 表单控件默认高度使用 `--ui-form-control-height`；同一筛选行的标签、输入框、选择器、日期范围和按钮应对齐，不用任意 margin 修补错位。
- 查询和提交使用主按钮；重置使用默认按钮；新增、修改、删除、导出等动作沿用现有语义色。仅图标即可准确表达的工具按钮使用图标并提供 tooltip 或 `aria-label`。
- 表格表头、行高、边框和悬停态由全局样式控制；长文本使用省略和 tooltip，操作列保持稳定宽度，空列表返回并展示空状态而不是塌陷布局。
- 标签、开关、复选框、单选框、分页、弹窗和抽屉使用 Arco Design Vue 组件，禁止用普通文本或自绘控件替代成熟交互。
- 列表查询、提交、上传和破坏性操作必须提供可见的进行中状态，防止重复触发；异步 `loading`、`submitting` 等状态统一在 `finally` 或等价的完成分支收口，成功、失败、空数据不得共用一种反馈。
- 图片、验证码、上传和 `iframe` 等异步媒体必须使用真实的 `load`、`error`、`success` 回调驱动加载态，并提供可识别的替代文本；不得用固定延时伪造加载完成。
- 破坏性操作必须二次确认；新增或调整操作权限时同步检查前端 `v-hasPermi`、后端 `@PreAuthorize`、`sys_menu` 的 `F` 菜单和必要的 `sys_role_menu` 默认授权，保证权限闭环。

### 响应式与可用性

- 必须检查至少 `1440px` 桌面、`991px` 平板和 `390px` 手机宽度；固定格式控件使用稳定尺寸或 `minmax`，文本不得遮挡、溢出按钮或挤压相邻操作。
- `991px` 以下侧栏切换为抽屉式，页面概览视觉可缩小；`640px` 以下隐藏纯装饰内容，筛选项和弹窗表单改为单列，表格允许横向滚动。
- 所有可点击图标必须具备可识别的悬停、键盘焦点和禁用状态；动画遵守 `prefers-reduced-motion`，不添加持续占用 CPU 的装饰动画。
- 自定义点击区域优先使用原生 `button`、链接或 Arco 交互组件；确需使用其他元素时，必须补齐语义 `role`、`tabindex`、`aria-label`，并同时支持 `Enter` 和 `Space` 键，不能只绑定鼠标点击。
- 弹窗宽度和多列表单必须按 Arco 生成的 `.arco-modal`、`.arco-col-*` 类实现移动端规则；不得只维护已移除组件库的 `.el-*` 选择器，并在 `390px` 下验证弹窗不超出视口、双列表单变为单列。

### 前端验收

- UI 改动完成后至少执行 `cd ui-admin-arco && pnpm run build`，并检查浏览器控制台无新增错误。
- 涉及壳层、主题或公共组件时，必须截图核对首页、一个标准列表页、一个特殊页和手机视口，确认侧栏、顶栏、页签、筛选区、表格、分页和弹窗没有重叠或视觉断层。
- 新增页面优先复用既有主题类和组件；若必须增加局部样式，只保留当前页面独有的布局，能由全局变量或公共类表达的样式不得复制到页面中。

## 构建、测试与本地运行

- `mvn clean package`：构建全部后端模块。
- `mvn test`：运行单元测试和 ArchUnit 架构测试。
- `mvn -pl manzhushaka-admin -am spring-boot:run`：启动后端服务。
- `cd ui-admin-arco && pnpm install --frozen-lockfile`：按 `pnpm-lock.yaml` 安装前端依赖。
- `cd ui-admin-arco && pnpm run dev`：启动 Vite 开发服务。
- `cd ui-admin-arco && pnpm run build`：构建生产前端包。
- 如果有代码变更，最好执行 `codegraph sync .` 更新索引。

## Java 命名规范

- 命名不得以下划线或美元符号开头或结尾；禁止中文命名、拼音与英文混用；国际通用专有名词可视同英文。
- 代码和注释中避免任何语言的歧视性词语。
- 类名使用 `UpperCamelCase`；`DO`、`BO`、`DTO`、`VO`、`AO`、`PO`、`UID` 等领域后缀保留大写。
- 方法名、参数名、成员变量、局部变量使用 `lowerCamelCase`。
- 常量全部大写，单词之间用下划线，语义必须完整。
- 抽象类以 `Abstract` 或 `Base` 开头；异常类以 `Exception` 结尾；测试类以被测类名开头、以 `Test` 结尾。
- 数组类型中括号跟随类型，例如 `String[] args`。
- POJO 布尔属性不要加 `is` 前缀，避免框架序列化或反射解析错误。
- 包名全小写，点分隔符之间只放一个自然语义英文单词，通常使用单数。
- 避免子父类成员变量重名，避免不同代码块局部变量重名。
- 杜绝不可读缩写，例如 `AbsClass`、`condi`、`Fu`。
- 变量和常量中表示类型的名词建议放在词尾，例如 `workQueue`、`nameList`。
- 使用设计模式时，在类名或方法名中体现模式和业务语义，例如 `OrderFactory`、`LoginProxy`。
- 接口方法和属性不要显式写 `public`，保持简洁并补充 Javadoc。
- Service 和 DAO 对外暴露接口，实现类使用 `Impl` 后缀；能力型接口可用形容词形式，例如 `Translatable`。
- 枚举类建议以 `Enum` 结尾，枚举成员全部大写并用下划线分隔。
- 分层方法命名建议：单对象查询用 `get`，列表查询用 `list`，统计用 `count`，新增用 `save` 或 `insert`，删除用 `remove` 或 `delete`，修改用 `update`。
- 领域模型命名使用 `DO`、`DTO`、`BO`、`AO`、`VO`、`Query`；禁止命名为 `xxxPOJO`。

## 常量定义

- 禁止魔法值直接出现在代码中，应定义为有语义的常量或枚举。
- `long` 或 `Long` 赋值使用大写 `L`，不要使用小写 `l`。
- 不要维护一个大而全的常量类，应按功能和边界拆分。
- 常量复用按范围分为跨应用、应用内、子工程内、包内、类内，范围越小越优先。
- 固定范围内变化的值建议使用 `enum`，尤其当枚举值还有扩展属性时。

## 代码格式

- 使用 4 个空格缩进，禁止 Tab；文件编码使用 UTF-8，换行符使用 Unix 格式。
- 空代码块可写成 `{}`；非空代码块左大括号不换行，左大括号后换行，右大括号前换行。
- 小括号内侧不加空格；左大括号前加空格。
- `if`、`for`、`while`、`switch`、`do` 等关键字与括号之间加空格。
- 二目、三目运算符左右各加一个空格。
- `//` 与注释内容之间有且仅有一个空格。
- 类型强制转换时，右括号和转换值之间不加空格。
- 单行不超过 120 个字符；换行时第二行缩进 4 个空格，运算符、点号随下文换行，多个参数在逗号后换行。
- 方法参数定义和传入时，逗号后加空格。
- 单个方法推荐不超过 80 行。
- 不要为了等号对齐添加多余空格。
- 不同逻辑、语义、业务代码之间用一个空行分隔，不使用多个连续空行。

## OOP 与类型规约

- 静态变量和静态方法使用类名访问，不通过对象引用访问。
- 覆写方法必须加 `@Override`。
- 只有相同参数类型、相同业务含义时才使用可变参数；可变参数必须放在最后，避免用 `Object...` 混淆语义。
- 外部调用或二方库依赖的接口不允许随意修改签名；废弃接口加 `@Deprecated` 并说明替代方案。
- 禁止使用过时类或方法。
- 调用 `equals` 时，使用常量或确定非空对象作为调用方，或使用 `Objects.equals`。
- 整型包装类之间值比较使用 `equals`，不要使用 `==`。
- 金额使用最小货币单位和整型类型存储。
- 浮点数等值判断不要用 `==` 或包装类 `equals`，应使用误差范围或 `BigDecimal`。
- `BigDecimal` 等值比较使用 `compareTo()`，禁止使用 `BigDecimal(double)` 构造精确金额。
- DO 属性类型要与数据库字段类型匹配，例如 `bigint` 对应 `Long`。
- POJO 属性、RPC 参数和返回值使用包装类型；局部变量推荐使用基本类型。
- DO、DTO、VO 等 POJO 不要设置默认值。
- 序列化类新增属性时不要随意修改 `serialVersionUID`；完全不兼容升级时才修改。
- 构造方法不要包含业务逻辑，初始化逻辑放入 `init`。
- POJO 必须实现 `toString()`；继承其他 POJO 时包含 `super.toString()`。
- POJO 中不要同时存在同一属性的 `isXxx()` 和 `getXxx()`。
- 使用 `String.split` 后按索引访问数组时，注意最后分隔符后是否有内容。
- 多个构造方法或同名方法应放在一起。
- 类内方法顺序推荐：公有/保护方法、私有方法、getter/setter。
- setter 参数名与成员变量名一致，使用 `this.field = field`；getter/setter 中不要加入业务逻辑。
- 循环体内字符串拼接使用 `StringBuilder.append`。
- 合理使用 `final` 限制继承、覆写、引用变更和重复赋值。
- 慎用 `clone`，默认浅拷贝容易引发问题。
- 类成员和方法访问控制从严，能 `private` 不 `protected`，能 `protected` 不 `public`。

## 数据模型编码规范

- 当前仓库后端数据模型默认采用显式手写 `getXxx`、`setXxx`、`toString()` 的方式，不默认引入或使用 Lombok。
- 持久化实体优先放在对应模块的 `infrastructure/persistence/entity` 目录；仓库仍保留部分 `domain` 旧模型，新增或迁移前先读取真实调用链路和同目录风格。
- `dto`、`query`、`request` 表示接口入参或查询条件，当前主要位于 `manzhushaka-admin/src/main/java/com/manzhushaka/web/dto` 和业务模块 `application/query`；默认手写访问器和必要的 `toString()`。
- `vo`、`response` 表示接口出参或页面展示模型，当前主要位于 `manzhushaka-admin/src/main/java/com/manzhushaka/web/vo` 和业务模块 `domain/vo`；默认手写访问器和必要的 `toString()`。
- 新增模型类或调整模型字段时，必须同步检查字段注释、getter、setter、`toString()`、XML `resultMap`、查询条件、转换器、VO 映射、前端接口字段和表单回写。
- 实体、DTO、VO 的 `toString()` 不得输出密码、Token、密钥、验证码、证件号、手机号、请求快照、响应快照、消息载荷等敏感或超长字段；必要时显式省略或脱敏。
- 当前仓库未使用 MyBatis-Plus 注解作为主映射方式；新增字段默认优先维护 MyBatis XML、实体属性和 SQL 脚本，除非任务已明确引入并验证 MyBatis-Plus 链路。
- 修改 `BaseEntity`、分页结果、通用选项项等基础模型字段时，要同步检查所有子类、映射和依赖该字段的业务逻辑，避免公共模型发生隐式破坏。

## 日期时间规约

- 日期格式化年份使用小写 `y`，例如 `yyyy-MM-dd HH:mm:ss`；大写 `Y` 是“周所属年份”，跨年周会出错。
- 区分 `M`/`m` 和 `H`/`h`：`M` 是月份，`m` 是分钟，`H` 是 24 小时制，`h` 是 12 小时制。
- 获取当前毫秒使用 `System.currentTimeMillis()`，不要使用 `new Date().getTime()`。
- 不使用 `java.sql.Date`、`java.sql.Time`、`java.sql.Timestamp`。
- 不要写死一年 365 天，应使用日期 API 处理闰年。
- 避免闰年 2 月 29 日带来的一年后日期问题。
- 推荐用枚举或常量表示月份；使用 `Date`、`Calendar` 时注意月份从 0 到 11。
- 并发场景不要静态共享 `SimpleDateFormat`，优先使用 `DateTimeFormatter`、`ThreadLocal` 或加锁。

## 集合处理规约

- 覆写 `equals` 必须覆写 `hashCode`；Set 元素和自定义对象作为 Map key 时都必须保证两者正确。
- 判断集合为空使用 `isEmpty()`，不要使用 `size() == 0`。
- `Collectors.toMap()` 必须处理重复 key，并注意 value 为 `null` 会触发 NPE。
- `ArrayList.subList()` 返回视图，不可强转为 `ArrayList`。
- `Map.keySet()`、`values()`、`entrySet()` 返回集合对象不可添加元素。
- `Collections.emptyList()`、`singletonList()` 等不可变集合不可添加或删除元素。
- 对父集合增删会影响 `subList`，可能触发 `ConcurrentModificationException`。
- 集合转数组使用 `toArray(T[] array)`；推荐传入类型一致、长度为 0 的空数组。
- `addAll()` 前判断输入集合是否为 `null`。
- `Arrays.asList()` 返回数组适配视图，不能调用 `add`、`remove`、`clear`。
- 泛型通配符遵循 PECS：`<? extends T>` 适合读取，不适合添加；`<? super T>` 适合写入，读取受限。
- 无泛型集合赋给泛型集合后，使用元素前要做 `instanceof` 判断。
- 不要在 foreach 中直接 add/remove，删除使用 `Iterator.remove()`；并发操作要加锁。
- `Comparator` 必须满足反对称性、传递性、一致性。
- JDK 7+ 推荐使用 diamond 语法。
- 集合初始化时指定容量，尤其是 `HashMap`、`ArrayList`。
- 遍历 Map 推荐使用 `entrySet`，避免 `keySet` 二次查询。
- 注意不同 Map 的 null 支持差异：`Hashtable` 和 `ConcurrentHashMap` 不允许 null key/value；`TreeMap` 不允许 null key；`HashMap` 允许 null key/value。
- 合理利用集合的有序性和稳定性；去重优先使用 Set，避免 List 反复 `contains`。

## 并发处理规约

- 单例对象和其中方法都要保证线程安全。
- 创建线程或线程池时指定有意义的名称。
- 线程资源必须由线程池提供，不要显式 `new Thread`。
- 线程池不要用 `Executors` 创建，应使用 `ThreadPoolExecutor` 明确队列、线程数、拒绝策略等参数，避免 OOM。
- 自定义 `ThreadLocal` 必须回收，尤其在线程池场景中要在 `finally` 中 `remove()`。
- 高并发下尽量减少锁粒度，不要在锁代码块中调用 RPC。
- 多资源加锁保持顺序一致，避免死锁。
- 阻塞锁应在 `try` 外加锁，`unlock` 放在 `finally` 第一行，避免无法释放。
- 尝试锁进入业务代码前必须判断是否拿到锁。
- 并发修改同一记录时要加锁，可在应用、缓存或数据库层使用乐观锁或悲观锁。
- 定时任务不要用 `Timer` 承载多个任务，推荐 `ScheduledExecutorService`。
- 金融敏感信息推荐悲观锁。
- `CountDownLatch.countDown()` 应确保执行，避免主线程一直等待到超时。
- 避免多线程共享 `Random` 或 `Math.random()`，可使用 `ThreadLocalRandom`。
- 双重检查锁延迟初始化需要 `volatile`。
- `volatile` 可解决一写多读的可见性，但不能保证多写复合操作原子性。
- 避免并发环境下 `HashMap` resize 死链风险。
- `ThreadLocal` 建议 `static` 修饰，但它不能解决共享对象更新问题。

## 控制语句规约

- `switch` 每个 `case` 要用 `continue`、`break`、`return` 等终止，或注释说明继续执行；必须包含 `default`，且放在最后。
- `switch` 变量是外部传入的 `String` 时，先判空。
- `if`、`else`、`for`、`while`、`do` 即使只有一行也必须使用大括号。
- 三目运算符注意两个表达式类型对齐，避免自动拆箱 NPE。
- 高并发场景避免用“等于”作为退出条件，推荐使用区间判断。
- 方法超过 10 行时，`return`、`throw` 等中断逻辑之后加空行。
- 异常分支少用深层 `if-else`，推荐卫语句、策略模式、状态模式；`if-else` 不应超过 3 层。
- 复杂条件先赋值给有意义的布尔变量。
- 不要在条件表达式等其他表达式中插入赋值语句。
- 循环体内注意性能，把对象创建、变量定义、数据库连接、不必要的 `try-catch` 等移到循环外。
- 避免取反逻辑运算符，尽量使用正向表达。
- 公开接口，尤其批量接口，需要入参保护。
- 调用频次低、执行开销大、稳定性要求高、对外接口、敏感权限入口等场景需要参数校验。
- 高频底层方法、循环内调用方法、确定只被可信私有方法调用的 `private` 方法，可在说明前提下减少重复校验。

## 注释规约

- 类、类属性、类方法使用 Javadoc 注释，不用 `// xxx` 替代。
- 抽象方法和接口方法必须有 Javadoc，说明功能、参数、返回值、异常，以及实现或调用注意事项。
- 每个新增或修改的方法都必须补充 Javadoc 注释；参数使用 `@param`，返回值使用 `@return`，异常按需使用 `@throws`。
- Javadoc 不允许使用 HTML 标签注释，特别禁止 `<p>`、`</p>`；需要分段时使用空行或简洁句子表达。
- 所有类添加创建者和创建日期；若项目已有统一模板，遵循项目模板。
- 业务方法、构造器、Mapper 自定义方法必须使用当前语言支持的标准注释语法补充说明；getter、setter、`toString()` 按同层现有显式实现风格保持一致。
- 方法内部单行注释放在被注释语句上方，多行注释使用 `/* */` 并与代码对齐。
- 枚举字段必须有注释说明用途。
- 注释可以用中文说清楚业务，专有名词和关键字保留英文。
- 修改代码时同步修改注释。
- 删除未使用字段、方法、内部类、参数、局部变量。
- 谨慎注释代码，确需保留时说明原因；无用代码直接删除。
- 注释应准确反映设计思想、代码逻辑和业务含义。
- 命名和结构自解释时，注释要精简准确，避免过多过滥。
- `TODO`、`FIXME` 需注明标记人、标记时间和预计处理时间，并定期清理。

## 前后端接口规约

- API 需明确协议、域名、路径、请求方法、请求内容、状态码和响应体。
- 生产环境使用 HTTPS。
- API 路径表示资源，推荐复数名词；路径小写，分隔用下划线；不要用 `.json`、`.xml` 等后缀表达内容类型。
- GET 获取资源，POST 新建资源，PUT 更新资源，DELETE 删除资源。
- URL 参数不得包含敏感信息；body 参数需设置 `Content-Type`。
- 数据列表为空时返回空数组 `[]` 或空集合 `{}`，不要返回 `null`。
- 服务端错误响应必须包含 HTTP 状态码、`errorCode`、`errorMessage` 和用户提示信息。
- JSON key 使用小写开头的 `lowerCamelCase`。
- `errorMessage` 用于排查问题，不应包含敏感数据。
- 超大整数服务端使用 `String` 返回，避免 JavaScript `Number` 精度损失。
- URL 参数长度不超过 2048 字节；body 内容要控制长度。
- 分页参数小于 1 返回第一页，大于总页数返回最后一页。
- 内部重定向用 `forward`，外部重定向通过统一 URL 代理模块生成。
- 服务端返回信息应标记缓存策略。
- 推荐 JSON，不推荐 XML。
- 前后端时间格式统一为 `yyyy-MM-dd HH:mm:ss`，统一 GMT。
- 接口路径不建议放版本号，版本可放 HTTP 头中。

## 菜单、按钮与接口权限约束

- 每个新增页面的菜单必须在 `sql/manzhushaka_db_init.sql` 的 `sys_menu` 初始化数据中登记，菜单权限字段使用 `perms`。
- 新增菜单时，按需同步 `sys_role_menu` 默认授权关系，确保初始化数据库后菜单可按预期访问。
- 每个页面上的业务按钮都必须配置权限字符串，前端使用 `v-hasPermi="['模块:资源:动作']"`，例如 `v-hasPermi="['system:user:add']"`。
- 按钮权限字符串必须与 `sys_menu.perms` 和后端接口权限保持一致；常见动作使用 `list`、`query`、`add`、`edit`、`remove`、`export`、`import`，不要在同域中混用 `update`、`delete` 等新风格。
- 每个需要鉴权的后端接口都必须声明权限字符串，使用 `@PreAuthorize("@ss.hasPermi('模块:资源:动作')")`。
- 接口权限字符串必须能在菜单 SQL 中找到对应的 `sys_menu.perms`，避免前端有按钮、后端有接口但初始化库无权限数据。
- 特殊接口如果确实需要匿名访问，必须显式添加 `@Anonymous` 注解，并在代码评审中说明匿名访问原因、风险和防滥用措施。
- 不允许新增未加 `@PreAuthorize` 且未加 `@Anonymous` 的裸接口；已有公开路径如登录、验证码、静态资源按安全配置处理。
- 新增权限时同步检查前端按钮、后端接口、`sys_menu.perms`、`sys_role_menu` 默认授权和接口文档，确保权限闭环。
- 当前菜单类型以 `menu_type` 单字符存储：`M` 表示目录，语义对应 `DIR`；`C` 表示页面菜单，语义对应 `MENU`；`F` 表示按钮权限，语义对应 `BUTTON`。
- 前端动态路由和侧边栏只渲染后端返回的 `M`、`C` 菜单，`F` 不生成路由、不参与侧边栏展示；按钮权限通过菜单树和角色关联中的 `perms` 进入权限集合后参与 `v-hasPermi` 判断。
- 新增页面时至少要有一个 `C` 菜单承载页面入口，配置路由名称、路由路径、前端组件路径和页面级权限标识；不要把页面入口建成 `F`。
- 新增页面内查询、新增、修改、删除、导出等操作时，默认在该 `C` 菜单下补齐对应 `F` 按钮和权限字符串；不要把纯按钮权限建成 `C`。
- 新建业务菜单默认先放到一级 `M` 目录下；如果当前业务没有合适的一级目录，先创建一个 `M` 目录，再在目录下创建业务 `C` 菜单。
- 新增菜单时默认联动检查 `sql/manzhushaka_db_init.sql`、后端 `menuType` 和 `perms`、父子关系、排序、显示状态、前端页面 `v-hasPermi`、后端接口 `@PreAuthorize` 和必要的角色默认授权。

## 其他编程要求

- 正则表达式应预编译，不要在方法体内反复 `Pattern.compile`。
- 避免使用 Apache BeanUtils 做属性复制；可考虑 Spring BeanUtils、Cglib BeanCopier，并注意浅拷贝问题。
- Velocity 访问 POJO 属性直接使用属性名。
- 后端传给页面的 Velocity 变量使用 `$!{var}`，避免 `null` 直接展示。
- `Math.random()` 返回 `[0,1)` 的 double，整数随机数使用 `Random.nextInt` 或 `nextLong`。
- 视图模板不要加入复杂逻辑，视图只负责展示。
- 数据结构构造或初始化应指定大小，避免无限增长耗尽内存。
- 及时清理不再使用的代码段或配置。

## 错误码规约

- 错误码用于快速溯源和沟通标准化，不能直接作为用户提示信息。
- 错误码不体现版本号和错误等级。
- 正常但必须填错误码时返回 `00000`。
- 错误码是 5 位字符串，由“错误来源 + 四位数字编号”组成。
- 编号不与业务架构或组织架构挂钩，统一审批后永久固定。
- 使用者不要随意定义新错误码。
- 业务特有信息放在 `error_message`，不要让错误码承载过多业务属性。
- 第三方服务错误码向上抛出时允许转义，并在错误信息中带原第三方错误码。
- 错误码后三位与 HTTP 状态码无关。

## 异常处理规约

- 先区分业务异常、编程错误和基础设施故障。用户可预期的校验失败、权限不足、状态冲突等场景，优先使用当前仓库业务异常类型 `ServiceException`，不要新增平行的 `BizException` 造成风格分裂。
- 能预检查规避的 `RuntimeException` 不要用 `catch` 处理，例如 NPE、数组越界。
- 异常捕获后不要用于流程控制或条件控制。
- `catch` 区分稳定代码和非稳定代码，非稳定代码尽量按异常类型分类处理。
- 捕获异常必须处理；不处理就向上抛；最外层业务使用者要转换为用户可理解信息。
- `catch` 范围尽量小且类型尽量具体；只有框架边界、任务调度、消息消费、统一异常翻译等场景才允许捕获较宽的 `Exception`，并立即记录日志、更新状态或重新抛出语义更明确的异常。
- 禁止吞异常。`catch` 后如果选择忽略，必须写注释说明原因，并确认不会影响状态一致性、排障或用户感知。
- 包装异常时必须保留原始 `cause`；如果当前异常类型无法保留根因，优先补齐构造器或改用支持 `cause` 的语义化异常类型。
- 事务中捕获异常后如需回滚，要手动回滚或继续抛出触发回滚。
- `finally` 必须关闭资源和流对象，异常也要处理；JDK 7+ 可用 try-with-resources。
- 不要在 `finally` 中 `return`。
- 捕获异常和抛出异常要匹配，或捕获其父类。
- checked exception 要么在当前层 `catch`，要么在方法签名中 `throws`；重写父类或接口方法时，不要扩大其声明的 checked exception 范围；`catch` 顺序必须先子类后父类。
- 调用 RPC、二方包、动态生成类方法时，必要时使用 `Throwable` 拦截类加载等错误。
- 方法返回 `null` 时必须注释说明场景；调用方仍需防 NPE。
- 防 NPE 关注自动拆箱、数据库查询、集合元素、远程调用、Session、链式调用等场景。
- 区分 checked 和 unchecked 异常，避免直接抛 `RuntimeException`、`Exception`、`Throwable`，应使用有业务含义的自定义异常。
- 对外 HTTP/API 使用 `errorCode`；应用内部推荐抛异常；跨应用 RPC 可优先使用 Result 模式。

## 日志规约

- `manzhushaka-admin/src/main/java/com/manzhushaka/web/controller` 下所有 Controller HTTP 接口方法必须添加操作日志注解 `@Log`，全限定名为 `com.manzhushaka.common.annotation.Log`。
- `@Log.title` 使用清晰的业务模块或操作名称，例如 `"用户管理"`、`"菜单管理"`。
- `@Log.businessType` 必须按实际动作选择 `com.manzhushaka.common.enums.BusinessType`，例如 `INSERT`、`UPDATE`、`DELETE`、`EXPORT`、`IMPORT`、`GRANT`、`CLEAN`、`FORCE`、`OTHER`。
- 涉及密码、Token、密钥、验证码、文件内容、请求快照、响应快照、大体量响应等敏感或超长数据时，必须通过 `isSaveRequestData = false`、`isSaveResponseData = false` 或 `excludeParamNames` 避免落库。
- 新增 Controller 接口时同步检查 `@PreAuthorize` 或 `@Anonymous` 与 `@Log` 是否同时满足规范，不能只补权限或只补日志。
- 不直接依赖 Log4j、Logback API，应使用 SLF4J、JCL 等日志门面。
- 日志至少保存 15 天；网络运行状态、网络安全事件、个人敏感信息操作等日志按合规要求留存并备份。
- 扩展日志命名使用 `appName_logType_logName.log`，例如 stats、monitor、access。
- 日志变量拼接使用占位符。
- `trace`、`debug`、`info` 输出前要判断日志级别开关。
- 避免重复打印日志，配置 `additivity=false`。
- 生产环境禁止 `System.out`、`System.err`、`e.printStackTrace()`。
- 异常日志要包含现场信息和异常堆栈；不处理则向上抛。
- 禁止直接用 JSON 工具把完整对象转成字符串写日志，避免性能问题和敏感信息泄露。
- 生产环境禁止 debug；谨慎使用 info；临时 warn 观察日志要注意量级并及时删除。
- 用户输入错误可用 warn，不必频繁打 error。
- 日志错误信息尽量英文；英文说不清楚时可用中文，避免歧义。

## 单元测试规范

- 单元测试遵守 AIR：Automatic、Independent、Repeatable。
- 单元测试必须自动执行、非交互，不能用 `System.out` 人工验证，必须使用 assert。
- 测试用例之间不能互相调用，也不能依赖执行顺序。
- 单元测试不能受外部环境影响，应通过依赖注入、Mock、本地实现隔离网络、服务和中间件。
- 测试粒度至多类级别，通常方法级别。
- 核心业务、核心应用、核心模块的增量代码必须确保单元测试通过。
- 单元测试代码放在 `src/test/java`，不能放业务代码目录。
- 推荐语句覆盖率 70%，核心模块语句覆盖率和分支覆盖率 100%。
- 编写测试遵守 BCDE：边界值、正确输入、设计文档、错误输入。
- 数据库测试不要假设已有数据，应通过程序插入或导入准备数据。
- 数据库测试可设置自动回滚，或使用明确前后缀标识测试数据。
- 不可测代码应重构到可测，不要为了覆盖率写不规范测试。
- 设计评审阶段应确定单元测试范围，最好覆盖所有测试用例。
- 单元测试应在提测前完成，不建议发布后补。
- 避免业务代码中构造方法过重、全局变量和静态方法过多、外部依赖过多、条件语句过多。
- 本仓库后端使用 Spring Boot Test、JUnit 5 和 ArchUnit；涉及模块边界变更时必须更新架构测试。
- 前端 `package.json` 暂无 `test` 脚本，UI 变更需执行生产构建、浏览器检查并提供截图。

## 安全规约

- 属于用户个人的页面或功能必须做权限控制校验，避免水平越权。
- 用户敏感数据禁止直接展示，必须脱敏。
- 新增或调整接口出参字段时，如果字段属于手机号、证件号、银行卡号、密码、Token、密钥等敏感信息，必须评估是否需要脱敏；需要展示时，默认在后端 VO 字段使用当前仓库的 `@Sensitive` 注解和合适的 `DesensitizedType`。
- 当前仓库没有 `@SensitiveField` 注解；不要把不存在的注解写入代码，除非本次任务同时实现并接入完整序列化链路。
- 配置文件中新增密码、密钥、Token 等敏感配置时，使用 `${XXXX:default}` 或 `${XXXX:}` 形式通过环境变量注入，不要把真实敏感值硬编码到配置文件。
- SQL 参数使用参数绑定或元数据字段限定，禁止拼接 SQL。
- 用户请求参数必须做有效性验证，防止超大分页、恶意排序、任意重定向、注入、ReDoS 等问题。
- 禁止向 HTML 输出未经安全过滤或未正确转义的用户数据。
- 表单、AJAX 提交必须做 CSRF 安全验证。
- URL 外部重定向目标地址必须做白名单过滤。
- 短信、邮件、电话、下单、支付等平台资源必须做防重放限制，例如数量限制、疲劳度控制、验证码。
- 发帖、评论、即时消息等 UGC 场景需要防刷和违禁词过滤。
- 不提交本地密钥、数据库密码、生产 Redis 配置、Token 或个人敏感信息。
- 运行前检查 `manzhushaka-admin/src/main/resources/application*.yml`。

## MySQL 建表规约

- 系统功能表使用 `sys_` 前缀，业务功能表使用 `biz_` 前缀，表名必须准确表达所属业务域。
- 表达是/否的字段命名为 `is_xxx`，类型为 `unsigned tinyint`，1 表示是，0 表示否。
- 表名、字段名用小写字母或数字，不能数字开头，不能出现两个下划线中间只有数字。
- 表名不用复数。
- 禁用 MySQL 保留字，例如 `desc`、`range`、`match`。
- 索引命名：主键 `pk_字段名`，唯一索引 `uk_字段名`，普通索引 `idx_字段名`。
- 小数类型使用 `decimal`，禁止 `float` 和 `double`。
- 长度几乎固定的字符串用 `char`。
- `varchar` 长度不超过 5000；更长用 `text`，并独立成表，避免影响索引效率。
- 表必备三字段：`id`、`create_time`、`update_time`。
- 表名推荐“业务名称_表的作用”。
- 库名尽量与应用名一致。
- 字段含义或状态变化时同步更新字段注释。
- 允许适当冗余字段提升查询性能，但应非频繁修改、非超长字符串或文本，并保证一致性。
- 单表超过 500 万行或 2 GB 才推荐分库分表，不要过早拆分。

## MySQL 索引规约

- 业务唯一字段，即使是组合字段，也必须建唯一索引。
- 超过 3 个表禁止 join；join 字段类型必须一致，被关联字段需要索引。
- `varchar` 字段建索引必须指定索引长度，根据区分度决定。
- 页面搜索禁止左模糊和全模糊，需要时使用搜索引擎。
- `order by` 场景利用索引有序性，排序字段放在组合索引最后。
- 使用覆盖索引避免回表。
- 超大分页使用延迟关联或子查询优化。
- SQL 性能目标至少达到 `range`，最好 `ref`，能 `consts` 更好；避免 `index` 全索引扫描。
- 组合索引通常把区分度最高字段放左边；等值和范围混合时等值字段前置。
- 防止字段类型不同导致隐式转换，进而索引失效。
- 避免“一个查询一个索引”“索引拖慢更新所以尽量不建”“唯一性只靠应用层”等误解。

## SQL 语句规约

- 不要用 `count(列名)` 或 `count(常量)` 替代 `count(*)`。
- `count(distinct col)` 只统计非 NULL；多列 distinct 中任一列全 NULL 可能返回 0。
- 某列全 NULL 时 `count(col)` 返回 0，`sum(col)` 返回 NULL，注意 NPE。
- 使用 `IS NULL` 或 `ISNULL()` 判断 NULL，不要直接用 `= NULL` 或 `<> NULL`。
- 分页查询 count 为 0 时直接返回，不再执行分页 SQL。
- 禁用外键与级联，外键关系在应用层解决。
- 禁用存储过程，避免难调试、难扩展、移植性差。
- 数据订正，尤其删除和修改，先 select 确认再执行。
- 多表查询或变更涉及多个表时，列名前加表别名或表名。
- SQL 表别名推荐加 `as`，并按 `t1`、`t2`、`t3` 命名。
- 尽量避免 `in`，无法避免时控制集合元素数量在 1000 以内。
- 国际化场景使用 `utf8` 字符集；如需存储表情，使用 `utf8mb4`。
- `TRUNCATE TABLE` 速度快但无事务且不触发 trigger，不建议在开发代码中使用。

## ORM 映射规约

- 查询字段列表不要用 `*`，需要哪些字段明确写明。
- POJO 布尔属性不能加 `is`，数据库字段必须加 `is_`，需要在 `resultMap` 做映射。
- 不要用 `resultClass` 作为返回参数，应定义映射关系。
- MyBatis 参数使用 `#{}`，不要用 `${}` 传普通参数，防止 SQL 注入。
- iBATIS 自带 `queryForList(String statementName, int start, int size)` 不推荐使用，因为它先查全量再内存截取。
- 不允许直接用 `HashMap`、`Hashtable` 作为查询结果输出。
- 更新记录必须同时更新 `update_time`。
- 不要写大而全的数据更新接口，只更新变化字段。
- 不要滥用 `@Transactional`，需考虑 QPS、缓存回滚、搜索引擎回滚、消息补偿、统计修正等。

## 工程分层规约

- 开放接口层封装 Service 为 RPC/HTTP 接口，处理网关安全和流量控制。
- 终端显示层负责模板、JS、移动端等展示。
- Web 层负责访问控制转发、基础参数校验和简单业务处理。
- Service 层负责具体业务逻辑。
- Manager 层负责第三方平台封装、Service 通用能力下沉、DAO 组合复用等通用业务处理。
- DAO 层负责数据访问，与 MySQL、Oracle、HBase 等交互。
- DAO 层可捕获异常后包装为 `DAOException`，通常不重复打日志。
- Service 层必须记录错误日志，并尽量带参数信息。
- Manager 层与 Service 同机时可按 DAO 处理，单独部署时按 Service 处理。
- Web 层不应继续向上抛异常，应返回友好错误页面和用户可理解提示。
- 开放接口层把异常转换为错误码和错误信息。

## 服务器与运行规约

- 高并发服务器调小 TCP `time_wait` 超时时间，例如 `net.ipv4.tcp_fin_timeout = 30`。
- 调大最大文件句柄数，避免高并发下 `open too many files`。
- JVM 设置 `-XX:+HeapDumpOnOutOfMemoryError`，便于 OOM 排查。
- 生产环境 JVM `Xms` 和 `Xmx` 设置相同，避免 GC 后调整堆大小。
- 内部重定向用 `forward`；外部重定向使用统一代理生成。

## 设计规约

- 存储方案和底层数据结构必须评审通过并沉淀文档。
- 需求分析阶段，如果用户类型超过 1 类且 User Case 超过 5 个，使用用例图表达需求。
- 业务对象状态超过 3 个时，用状态图表达状态及转换条件。
- 功能调用链路涉及对象超过 3 个时，用时序图明确输入输出。
- 模型类超过 5 个且依赖复杂时，用类图表达关系。
- 超过 2 个对象协作且流程复杂时，用活动图表达。
- 架构设计要明确系统边界、模块关系、后续演化原则、非功能需求。
- 需求和设计不仅考虑主流程，也要充分评估异常流程和业务边界。
- 类设计遵守单一职责。
- 谨慎使用继承，优先聚合/组合；必须继承时遵循里氏替换原则。
- 依赖抽象类和接口，遵循依赖倒置。
- 对扩展开放，对修改关闭。
- 抽取共性业务和公共行为，避免重复代码，遵守 DRY。
- 设计文档用于明确需求、理顺逻辑、便于后期维护，不只是指导编码。
- 可扩展性的本质是找到变化点并隔离变化点。
- 无障碍设计要考虑 Tab 聚焦顺序、验证码替代方式、自定义控件交互类型。

## 设计模式选择建议

- 多个算法、规则、渠道可替换时，优先考虑策略模式，例如 `DiscountStrategy`、`WechatPayStrategy`。
- 业务对象状态多且迁移规则复杂时，优先考虑状态模式，例如 `OrderState`。
- 对象创建逻辑复杂，调用方不应关心具体实现类时，使用工厂模式，例如 `PaymentFactory`。
- 对外隐藏一组复杂子系统调用时，使用门面模式，例如 `OrderFacade`。
- 旧接口或第三方接口与现有接口不兼容时，使用适配器模式。
- 不改原类但需要增强访问控制、日志、远程调用等行为时，使用代理模式。
- 不改原类但需要一层层叠加功能时，使用装饰器模式。
- 多个对象需要响应同一个事件时，使用观察者模式。
- 流程骨架固定、部分步骤允许定制时，使用模板方法模式。
- 某类对象全局只应有一个实例且状态安全时，才使用单例模式。
- 不要为了“看起来高级”而套模式；业务语义必须比模式名更清楚。

## PMD 与自动化检查

- 推荐接入 P3C PMD 规则集，覆盖命名、常量、OOP、集合、并发、控制语句、注释、异常、ORM 和其他规则。
- 重点命名规则：类名 `UpperCamelCase`，变量和方法 `lowerCamelCase`，布尔属性不以 `is` 开头，数组括号跟随类型，包名小写，接口实现类使用 `Impl`。
- 重点常量规则：`long` 使用大写 `L`，禁止魔法值。
- 重点 OOP 规则：`equals` 防 NPE，包装类型比较使用 `equals`，POJO 使用包装类型且不设默认值，POJO 实现 `toString()`，循环中字符串拼接使用 `StringBuilder`，禁止 `BigDecimal(double)`。
- 重点集合规则：集合转数组使用 `toArray(T[])`，不要修改 `Arrays.asList` 结果，`subList` 不强转，foreach 中不修改集合，集合初始化指定容量。
- 重点并发规则：不要用 `Executors` 创建线程池，不使用 `Timer`，不显式创建线程，线程设置名称，避免静态共享 `SimpleDateFormat`，`ThreadLocal` 要 `remove()`，避免共享 `Random`，`CountDownLatch.countDown()` 要确保执行，Lock 使用 try-finally。
- 重点控制规则：`switch` 有终止和 `default`，控制语句必须有大括号，复杂条件抽取变量，避免取反逻辑。
- 重点注释规则：类、属性、方法使用 Javadoc，抽象方法和接口方法必须有 Javadoc，类有作者信息，枚举字段有注释，单行注释放代码上方，清理注释代码。
- 重点异常规则：包装类型自动拆箱防 NPE，`finally` 中不 return，事务异常需要回滚。
- 重点其他规则：正则预编译，避免 Apache BeanUtils，获取毫秒用 `System.currentTimeMillis()`，随机整数不用 `Math.random()` 放大取整，方法不超过 80 行，日期格式大小写正确，浮点数不直接比较。
- 格式化遵循 P3C：4 空格缩进、Tab 转空格、行宽 120、运算符空格、逗号后空格、分号前不加空格。

## 提交与 Pull Request

- 提交信息遵循现有 Conventional Commit 风格，例如 `feat(ui-admin-arco): add theme tokens`、`refactor: remove common coupling`、`test: add architecture guardrails`。
- PR 需说明变更范围、影响模块、验证命令和关联 Issue。
- 涉及 UI 时提供截图；涉及数据库时同步 `sql` 脚本和回滚说明；涉及配置时说明本地与生产差异。
- 合并前至少运行相关后端测试；架构边界、数据库、安全、前后端契约和异常日志规则无法完全依赖静态扫描，必须在评审中人工确认。

## Agent 协作要求

- 修改前先阅读相关模块、测试和配置；先说明关键假设、风险和验证目标。
- 保持改动最小、局部、可验证；每一行修改都应能追溯到用户请求。
- 不回滚用户已有改动，不做无关重构，不随意移动模块边界。
- 新增 Java 代码同时考虑 P3C 规则、模块边界、单元测试、安全输入校验和日志脱敏。
- 新增 SQL 同步考虑建表、索引、分页、ORM 映射、初始化脚本和回滚说明。
- 新增前端接口调用同步确认路径、方法、参数、错误响应、空列表、超大整数处理和按钮权限字符串。
- 新增接口同步确认 `@PreAuthorize` 权限字符串或 `@Anonymous` 匿名注解，禁止出现权限状态不明的接口。
- 出现准备新增框架、依赖、全局配置，diff 触及无关文件，修复依赖尚未验证的假设，或还没验证就准备宣称完成等信号时，先停下来复查。
- 完成后报告改动文件、验证命令、系统能自动支持的检查项和仍需人工确认的风险。

## Redis Stream MQ 使用与开发规范

### 架构总览

本仓库使用 Redis Stream 作为异步消息中间件，采用"框架层基础设施 + 系统层台账记录 + 模板方法处理器"的架构。相关文件统一放在以下路径：


| 层                                 | 模块                    | 包路径                                                   |
| ---------------------------------- | ----------------------- | -------------------------------------------------------- |
| 基础设施（网关、发布器、处理框架） | `manzhushaka-framework` | `com.manzhushaka.framework.mq`                           |
| 配置（容器 Bean、调度开关）        | `manzhushaka-framework` | `com.manzhushaka.framework.config`                       |
| 台账（实体、Mapper、Service）      | `manzhushaka-system`    | 现有`domain`、`mapper`、`service` 目录                   |
| 管理接口（Controller）             | `manzhushaka-admin`     | `com.manzhushaka.web.controller.monitor`                 |
| 前端页面                           | `ui-admin-arco`         | `api/monitor/mqLog.ts` + `views/monitor/mqLog/index.vue` |

### 核心概念与术语


| 术语                              | 含义                                                            |
| --------------------------------- | --------------------------------------------------------------- |
| Stream                            | Redis Stream 的 Key，每种消息类型一个独立的 Stream              |
| Consumer Group                    | 每个 Stream 对应的消费者组，使用 Stream 名称作为组名            |
| 消息类型 (`messageType`)          | 每种消息类型对应一个 Stream、一个 Handler、一套重试/死信策略    |
| 台账 (`SysMqMessageLog`)          | 每条消息的处理记录的主表，记录消息元数据、状态和重试信息        |
| 明细 (`SysMqMessageLogDetail`)    | 每次尝试处理的详细记录，包括开始时间、耗时、错误消息            |
| 重试流 (`mq:retry:{messageType}`) | 保存需要重试的消息，`nextRetryTime` 到达后重新投递到原始 Stream |
| 死信流 (`mq:dead:{messageType}`)  | 保存超过最大重试次数的消息，仅作归档不自动处理                  |

### 消息状态流转

```text
消息发布到 Stream → new
    ↓
Handler 接收到消息 → PROCESSING (0)
    ├── 处理成功 → SUCCESS (1) → ACK
    ├── 幂等跳过 → SKIPPED (3) → ACK
    ├── 处理失败（未超最大重试）
    │     └── 写入重试流 `mq:retry:{type}` → RetryScheduler 5s 后重新投递
    │         └── 再次消费 → 循环
    └── 处理失败（超过最大重试）→ DEAD_LETTER (4) → ACK
```

### 框架层基础设施

#### RedisStreamGateway ([`RedisStreamGateway.java`](manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamGateway.java))

网关接口封装了对 Redis Stream 的 5 个核心操作：

- `add(streamKey, body)`：向 Stream 追加消息，返回消息 ID
- `acknowledge(streamKey, group, messageId)`：确认消费完成
- `createGroupIfAbsent(streamKey, group)`：创建消费者组，已存在时不抛异常
- `range(streamKey, count)`：反向读取 Stream 中最近的 N 条消息
- `delete(streamKey, messageId)`：从 Stream 删除消息

实现类 `RedisStreamGatewayImpl` 使用 `RedisTemplate<Object, Object>` 操作。**注意**：`range()` 方法在 Spring Data Redis 4 中使用 `Range.unbounded()` 而非 `StreamOffset`，且因泛型兼容性需要使用 raw types。

#### RedisStreamMessagePublisher ([`RedisStreamMessagePublisher.java`](manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessagePublisher.java))

发布器组件通过 `publish(streamKey, messageType, businessKey, payload)` 构造标准 body，并调用 `gateway.add()` 写入 Stream。发布的 body 固定包含以下字段：

```text
messageType  — 消息类型（用于路由到对应的 Handler）
businessKey  — 业务唯一标识（用于幂等判断）
payload      — 消息内容体
retryTimes   — 重试次数（初始为 "0"，由 RetryScheduler 递增）
```

### 模板方法处理器

#### RedisStreamMessageHandler 接口 ([`RedisStreamMessageHandler.java`](manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageHandler.java))

所有消息处理器必须实现此接口，定义以下契约：

- `messageType()`：消息类型，必须全局唯一
- `streamKey()`：对应的 Redis Stream Key，必须全局唯一
- `consumerGroup()`：消费者组名
- `consumerName()`：消费者名称（用于台账明细记录）
- `maxRetryTimes()`：最大重试次数（默认 3），可覆写
- `immediateRetryTimes()`：立即重试次数（默认 0），可覆写
- `retryIntervalSeconds()`：重试间隔秒数（默认 60），可覆写
- `retryStreamKey()`：重试流 Key，默认 `mq:retry:{messageType}`
- `deadLetterStreamKey()`：死信流 Key，默认 `mq:dead:{messageType}`
- `handle(RedisStreamRecord)`：处理器主入口

#### AbstractRedisStreamMessageHandler 抽象基类 ([`AbstractRedisStreamMessageHandler.java`](manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/AbstractRedisStreamMessageHandler.java))

提供完整的模板方法实现 `handle()`，调用链如下：

1. 构建 `SysMqMessageLog` 对象
2. 调用 `createOrGetMessageLog` 避免重复插入（处理 `DuplicateKeyException`）
3. 调用 `isAlreadyProcessed()` 判断是否已成功处理过（可覆写）
4. 已处理 → 写 SKIPPED 明细 → ACK
5. 未处理 → 创建 PROCESSING 明细
6. 调用 `doHandle(record)` 执行业务逻辑（子类必须实现）
7. 成功 → 更新明细+主表状态为 SUCCESS → ACK
8. 失败（未超最大重试）→ 写入 retry stream → 更新主表状态为 FAILED → ACK
9. 失败（超最大重试）→ 写入 dead letter stream → 更新主表为 DEAD_LETTER → ACK

**子类需实现的方法**：

```java
@Override
public String messageType() { return "demo"; }
@Override
public String streamKey() { return "mq:stream:demo"; }
@Override
public String consumerGroup() { return "mq:stream:demo"; }
@Override
public String consumerName() { return "demoConsumer"; }

@Override
protected String idempotentKey(RedisStreamRecord record) {
    return record.getBodyValue("businessKey");
}

@Override
protected boolean isAlreadyProcessed(RedisStreamRecord record) {
    // 可选覆写，默认基于 businessKey 查台账状态
    return false;
}

@Override
protected void doHandle(RedisStreamRecord record) {
    String payload = record.getBodyValue("payload");
    // 业务处理逻辑
}
```

### 处理器注册与监听

#### RedisStreamMessageHandlerRegistry ([`RedisStreamMessageHandlerRegistry.java`](manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageHandlerRegistry.java))

- 构造时自动收集所有 `RedisStreamMessageHandler` Bean
- 构建 `streamKey → Handler` 和 `messageType → Handler` 两个索引
- 启动时检查 `messageType` 和 `streamKey` 是否全局唯一，重复则抛出 `IllegalStateException`

#### RedisStreamMessageListenerRegistrar ([`RedisStreamMessageListenerRegistrar.java`](manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageListenerRegistrar.java))

- 实现 `SmartLifecycle`，在 Spring 容器启动完成后自动注册所有 Listener
- 为每个 Handler 执行 `createGroupIfAbsent()`（幂等）
- 注册 `StreamMessageListenerContainer.receive()` 回调并转交 Handler；ACK 由 `AbstractRedisStreamMessageHandler` 在跳过、成功、转入重试或死信后显式执行
- 应用关闭时自动取消订阅并停止容器

#### RedisStreamRetryScheduler ([`RedisStreamRetryScheduler.java`](manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamRetryScheduler.java))

- `@Scheduled(fixedDelay = 5000L)` 每隔 5 秒扫描一次
- 遍历所有 Handler 的 retryStreamKey
- 检查 `nextRetryTime` 是否到达（`System.currentTimeMillis()` 比较）
- 到达 → 复制 body 并移除 `nextRetryTime` → 重新投递到原始 Stream → 从 retry stream 删除
- **注意**：`record.getBody()` 返回 `Collections.unmodifiableMap`，操作前必须 `new HashMap<>(record.getBody())` 复制为可变 Map

### 配置

在 [`RedisStreamMqConfig.java`](manzhushaka-framework/src/main/java/com/manzhushaka/framework/config/RedisStreamMqConfig.java) 中定义：

- `@Configuration` + `@EnableScheduling` 启用调度
- `StreamMessageListenerContainer<String, MapRecord<String, String, String>>` Bean，`pollTimeout` 为 2 秒

### 台账模型

#### SysMqMessageLog（主台账实体）


| 字段               | 类型    | 说明                 |
| ------------------ | ------- | -------------------- |
| `messageLogId`     | Long    | 主键                 |
| `messageType`      | String  | 消息类型             |
| `streamKey`        | String  | Stream 名称          |
| `messageId`        | String  | Redis Stream 消息 ID |
| `consumerGroup`    | String  | 消费者组             |
| `businessKey`      | String  | 业务 Key（用于幂等） |
| `payload`          | String  | 消息内容             |
| `status`           | String  | 状态码（枚举值）     |
| `retryTimes`       | Integer | 当前重试次数         |
| `maxRetryTimes`    | Integer | 最大重试次数         |
| `firstConsumeTime` | Date    | 首次消费时间         |
| `lastConsumeTime`  | Date    | 最后消费时间         |
| `successTime`      | Date    | 成功时间             |
| `deadLetterTime`   | Date    | 死信时间             |
| `lastErrorMsg`     | String  | 最后错误消息         |

**约束**：`toString()` 必须截断 `payload` 和 `lastErrorMsg` 字段，防止超长内容撑爆日志。

#### SysMqMessageLogDetail（明细实体）


| 字段           | 类型    | 说明         |
| -------------- | ------- | ------------ |
| `detailId`     | Long    | 主键         |
| `messageLogId` | Long    | 关联主台账   |
| `attemptNo`    | Integer | 尝试次数序号 |
| `consumerName` | String  | 消费者名称   |
| `status`       | String  | 状态码       |
| `startTime`    | Date    | 开始时间     |
| `endTime`      | Date    | 结束时间     |
| `costTime`     | Long    | 耗时（毫秒） |
| `errorMsg`     | String  | 错误消息     |

### 开发流程：新增一个消息类型

**步骤 1**：实现 Handler

在任意 Spring Bean 可扫描的模块中创建 Handler 类，继承 `AbstractRedisStreamMessageHandler`：

```java
@Component
public class DemoMessageHandler extends AbstractRedisStreamMessageHandler
{
    public DemoMessageHandler(RedisStreamGateway gateway, ISysMqMessageLogService mqMessageLogService) {
        super(gateway, mqMessageLogService);
    }

    @Override public String messageType() { return "demo"; }
    @Override public String streamKey() { return "mq:stream:demo"; }
    @Override public String consumerGroup() { return "mq:stream:demo"; }
    @Override public String consumerName() { return "demoConsumer"; }

    @Override
    protected String idempotentKey(RedisStreamRecord record) {
        return record.getBodyValue("businessKey");
    }

    @Override
    protected void doHandle(RedisStreamRecord record) {
        // 业务处理
    }
}
```

**步骤 2**：发布消息

```java
@Autowired
private RedisStreamMessagePublisher publisher;

public void sendDemoMessage(String businessKey, String payload) {
    publisher.publish("mq:stream:demo", "demo", businessKey, payload);
}
```

**步骤 3**：按本文件"菜单、按钮与接口权限约束"章节规范，新增菜单权限和 SQL 初始化脚本。

### 幂等性设计

- 主台账表在 `stream_key` + `message_id` 上有唯一索引，防止同一条消息被重复插入台账
- `createOrGetMessageLog()` 捕获 `DuplicateKeyException` 后重新查询返回已有记录
- `isAlreadyProcessed()` 钩子允许子类自定义幂等判断，默认基于 `businessKey` 查询台账状态
- 如果消息已成功（SUCCESS）或已死信（DEAD_LETTER），直接写 SKIPPED 明细并 ACK

### 开发约束

- 每个 `messageType` 和 `streamKey` 必须全局唯一，注册阶段会校验，重复则报错
- `AbstractRedisStreamMessageHandler.handle()` 中捕获 `Exception` 属于框架边界场景（第 333 条规约允许），但必须在 catch 块中记录日志并更新台账状态
- `RedisStreamRecord.getBody()` 返回不可修改的 Map，不允许直接调用 `remove()` 或 `put()`
- 重试调度中的 body 操作必须 `new HashMap<>(record.getBody())` 复制后再修改
- `payload`、`lastErrorMsg`、`errorMsg` 等字段在 `toString()` 中必须截断，避免打印超长内容
- Controller 层接口必须同步添加 `@Log` 和 `@PreAuthorize`，前端页面同步添加 `v-hasPermi`
- 新增 MQ 相关 SQL（建表、菜单、权限）必须同步维护 `sql/manzhushaka_db_init.sql`

### 系统支持边界补充

- 当前 Agent 可支持：检查 Redis Stream MQ 的 `@Component` Handler 是否遗漏 `messageType()`/`streamKey()` 实现、检查 Handler 是否被 `RedisStreamMessageHandlerRegistry` 自动收集（通过验证 Spring 注解）、检查台账 `toString()` 字段截断、检查权限字符串闭合。
- 当前无法完全自动保证：Handler 中的 `doHandle()` 业务逻辑正确性、重试间隔秒数是否合理、死信后的人工处理机制、Stream 消息体与台账字段的映射一致性。

## 系统支持边界

- 当前 Agent 可支持：读取真实调用链路、对照 `AGENTS.md`、检查 git diff、运行 `rg`、Maven 测试、前端构建和本地脚本，辅助发现权限、SQL、脱敏、异常和模型字段的显性遗漏。
- 当前项目可支持：`@PreAuthorize` 接口鉴权、`@Anonymous` 匿名声明、`v-hasPermi` 按钮显隐、`M/C/F` 菜单路由过滤、`@Sensitive` JSON 脱敏、全局 `ServiceException` 处理、MyBatis XML 映射和 `sql/manzhushaka_db_init.sql` 初始化。
- 当前无法完全自动保证：业务语义正确性、生产配置真实性、初始化库角色授权是否符合实际组织策略、敏感字段是否已被产品允许展示、权限码命名是否满足所有运营约定、UI 手工体验和跨环境数据库迁移风险。
- 当前不支持或未接入：`sql/manzhushaka_init.sql` 作为现有主初始化脚本、`@SensitiveField` 注解、`v-permission` 指令、`DIR/MENU/BUTTON` 字符串落库、默认 MyBatis-Plus 实体注解、默认 Lombok 模型生成。
