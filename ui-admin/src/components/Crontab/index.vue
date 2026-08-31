<template>
    <div>
        <a-tabs type="card">
            <a-tab-pane title="秒" v-if="shouldHide('second')">
                <CrontabSecond
                    @update="updateCrontabValue"
                    :check="checkNumber"
                    :cron="crontabValueObj"
                    ref="cronsecond"
                />
            </a-tab-pane>

            <a-tab-pane title="分钟" v-if="shouldHide('min')">
                <CrontabMin
                    @update="updateCrontabValue"
                    :check="checkNumber"
                    :cron="crontabValueObj"
                    ref="cronmin"
                />
            </a-tab-pane>

            <a-tab-pane title="小时" v-if="shouldHide('hour')">
                <CrontabHour
                    @update="updateCrontabValue"
                    :check="checkNumber"
                    :cron="crontabValueObj"
                    ref="cronhour"
                />
            </a-tab-pane>

            <a-tab-pane title="日" v-if="shouldHide('day')">
                <CrontabDay
                    @update="updateCrontabValue"
                    :check="checkNumber"
                    :cron="crontabValueObj"
                    ref="cronday"
                />
            </a-tab-pane>

            <a-tab-pane title="月" v-if="shouldHide('month')">
                <CrontabMonth
                    @update="updateCrontabValue"
                    :check="checkNumber"
                    :cron="crontabValueObj"
                    ref="cronmonth"
                />
            </a-tab-pane>

            <a-tab-pane title="周" v-if="shouldHide('week')">
                <CrontabWeek
                    @update="updateCrontabValue"
                    :check="checkNumber"
                    :cron="crontabValueObj"
                    ref="cronweek"
                />
            </a-tab-pane>

            <a-tab-pane title="年" v-if="shouldHide('year')">
                <CrontabYear
                    @update="updateCrontabValue"
                    :check="checkNumber"
                    :cron="crontabValueObj"
                    ref="cronyear"
                />
            </a-tab-pane>
        </a-tabs>

        <div class="popup-main">
            <div class="popup-result">
                <p class="title">时间表达式</p>
                <table>
                    <thead>
                        <tr>
                            <th v-for="item of tabTitles" :key="item">{{item}}</th>
                            <th>Cron 表达式</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>
                                <span v-if="crontabValueObj.second.length < 10">{{crontabValueObj.second}}</span>
                                <a-tooltip v-else :content="crontabValueObj.second" position="top"><span>{{crontabValueObj.second}}</span></a-tooltip>
                            </td>
                            <td>
                                <span v-if="crontabValueObj.min.length < 10">{{crontabValueObj.min}}</span>
                                <a-tooltip v-else :content="crontabValueObj.min" position="top"><span>{{crontabValueObj.min}}</span></a-tooltip>
                            </td>
                            <td>
                                <span v-if="crontabValueObj.hour.length < 10">{{crontabValueObj.hour}}</span>
                                <a-tooltip v-else :content="crontabValueObj.hour" position="top"><span>{{crontabValueObj.hour}}</span></a-tooltip>
                            </td>
                            <td>
                                <span v-if="crontabValueObj.day.length < 10">{{crontabValueObj.day}}</span>
                                <a-tooltip v-else :content="crontabValueObj.day" position="top"><span>{{crontabValueObj.day}}</span></a-tooltip>
                            </td>
                            <td>
                                <span v-if="crontabValueObj.month.length < 10">{{crontabValueObj.month}}</span>
                                <a-tooltip v-else :content="crontabValueObj.month" position="top"><span>{{crontabValueObj.month}}</span></a-tooltip>
                            </td>
                            <td>
                                <span v-if="crontabValueObj.week.length < 10">{{crontabValueObj.week}}</span>
                                <a-tooltip v-else :content="crontabValueObj.week" position="top"><span>{{crontabValueObj.week}}</span></a-tooltip>
                            </td>
                            <td>
                                <span v-if="crontabValueObj.year.length < 10">{{crontabValueObj.year}}</span>
                                <a-tooltip v-else :content="crontabValueObj.year" position="top"><span>{{crontabValueObj.year}}</span></a-tooltip>
                            </td>
                            <td class="result">
                                <span v-if="crontabValueString.length < 90">{{crontabValueString}}</span>
                                <a-tooltip v-else :content="crontabValueString" position="top"><span>{{crontabValueString}}</span></a-tooltip>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <CrontabResult :ex="crontabValueString"></CrontabResult>

            <div class="pop_btn">
                <a-button type="primary" @click="submitFill">确定</a-button>
                <a-button status="warning" @click="clearCron">重置</a-button>
                <a-button @click="hidePopup">取消</a-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import CrontabSecond from "./second.vue"
import CrontabMin from "./min.vue"
import CrontabHour from "./hour.vue"
import CrontabDay from "./day.vue"
import CrontabMonth from "./month.vue"
import CrontabWeek from "./week.vue"
import CrontabYear from "./year.vue"
import CrontabResult from "./result.vue"
const { proxy } = getCurrentInstance()
const emit = defineEmits(['hide', 'fill'])
const props = defineProps({
    hideComponent: {
        type: Array,
        default: () => [],
    },
    expression: {
        type: String,
        default: ""
    }
})
const tabTitles = ref(["秒", "分钟", "小时", "日", "月", "周", "年"])
const tabActive = ref(0)
const hideComponent = ref([])
const expression = ref('')
const crontabValueObj = ref({
    second: "*",
    min: "*",
    hour: "*",
    day: "*",
    month: "*",
    week: "?",
    year: "",
})
const crontabValueString = computed(() => {
    const obj = crontabValueObj.value
    return obj.second
        + " "
        + obj.min
        + " "
        + obj.hour
        + " "
        + obj.day
        + " "
        + obj.month
        + " "
        + obj.week
        + (obj.year === "" ? "" : " " + obj.year)
})
watch(expression, () => resolveExp())
function shouldHide(key) {
    return !(hideComponent.value && hideComponent.value.includes(key))
}
function resolveExp() {
    // 反解析 表达式
    if (expression.value) {
        const arr = expression.value.split(/\s+/)
        if (arr.length >= 6) {
            //6 位以上是合法表达式
            let obj = {
                second: arr[0],
                min: arr[1],
                hour: arr[2],
                day: arr[3],
                month: arr[4],
                week: arr[5],
                year: arr[6] ? arr[6] : ""
            }
            crontabValueObj.value = {
                ...obj,
            }
        }
    } else {
        // 没有传入的表达式 则还原
        clearCron()
    }
}
// tab切换值
function tabCheck(index) {
    tabActive.value = index
}
// 由子组件触发，更改表达式组成的字段值
function updateCrontabValue(name, value, from) {
    crontabValueObj.value[name] = value
}
// 表单选项的子组件校验数字格式（通过-props传递）
function checkNumber(value, minLimit, maxLimit) {
    // 检查必须为整数
    value = Math.floor(value)
    if (value < minLimit) {
        value = minLimit
    } else if (value > maxLimit) {
        value = maxLimit
    }
    return value
}
// 隐藏弹窗
function hidePopup() {
    emit("hide")
}
// 填充表达式
function submitFill() {
    emit("fill", crontabValueString.value)
    hidePopup()
}
function clearCron() {
    // 还原选择项
    crontabValueObj.value = {
        second: "*",
        min: "*",
        hour: "*",
        day: "*",
        month: "*",
        week: "?",
        year: "",
    }
}
onMounted(() => {
    expression.value = props.expression
    hideComponent.value = props.hideComponent
})
</script>

<style lang="scss" scoped>
.pop_btn {
    text-align: center;
    margin-top: 20px;
}
.popup-main {
    position: relative;
    margin: 10px auto;
    border-radius: 5px;
    font-size: 12px;
    overflow: hidden;
}
.popup-title {
    overflow: hidden;
    line-height: 34px;
    padding-top: 6px;
    background: var(--ui-bg-panel-muted);
}
.popup-result {
    box-sizing: border-box;
    line-height: 24px;
    margin: 25px auto;
    padding: 15px 10px 10px;
    border: 1px solid var(--ui-border-control);
    position: relative;
}
.popup-result .title {
    position: absolute;
    top: -28px;
    left: 50%;
    width: 140px;
    font-size: 14px;
    margin-left: -70px;
    text-align: center;
    line-height: 30px;
    background: var(--ui-bg-panel);
}
.popup-result table {
    text-align: center;
    width: 100%;
    margin: 0 auto;
}
.popup-result table td:not(.result) {
    width: 3.5rem;
    min-width: 3.5rem;
    max-width: 3.5rem;
}
.popup-result table span {
    display: block;
    width: 100%;
    font-family: var(--ui-font-family);
    line-height: 30px;
    height: 30px;
    white-space: nowrap;
    overflow: hidden;
    border: 1px solid var(--ui-border-subtle);
}
.popup-result-scroll {
    font-size: 12px;
    line-height: 24px;
    height: 10em;
    overflow-y: auto;
}
</style>
