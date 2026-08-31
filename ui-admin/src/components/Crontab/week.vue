<template>
    <a-form>
        <a-form-item>
            <a-radio v-model='radioValue' :value="1">
                周，允许的通配符[, - * ? / L #]
            </a-radio>
        </a-form-item>

        <a-form-item>
            <a-radio v-model='radioValue' :value="2">
                不指定
            </a-radio>
        </a-form-item>

        <a-form-item>
            <a-radio v-model='radioValue' :value="3">
                周期从
                <a-select allow-clear v-model="cycle01">
                    <a-option
                        v-for="(item,index) of weekList"
                        :key="index"
                        :label="item.value"
                        :value="item.key"
                        :disabled="item.key === 7"
                    >{{item.value}}</a-option>
                </a-select>
                -
                <a-select allow-clear v-model="cycle02">
                    <a-option
                        v-for="(item,index) of weekList"
                        :key="index"
                        :label="item.value"
                        :value="item.key"
                        :disabled="item.key <= cycle01"
                    >{{item.value}}</a-option>
                </a-select>
            </a-radio>
        </a-form-item>

        <a-form-item>
            <a-radio v-model='radioValue' :value="4">
                第
                <a-input-number v-model='average01' :min="1" :max="4" /> 周的
                <a-select allow-clear v-model="average02">
                    <a-option v-for="item in weekList" :key="item.key" :label="item.value" :value="item.key" />
                </a-select>
            </a-radio>
        </a-form-item>

        <a-form-item>
            <a-radio v-model='radioValue' :value="5">
                本月最后一个
                <a-select allow-clear v-model="weekday">
                    <a-option v-for="item in weekList" :key="item.key" :label="item.value" :value="item.key" />
                </a-select>
            </a-radio>
        </a-form-item>

        <a-form-item>
            <a-radio v-model='radioValue' :value="6">
                指定
                <a-select class="multiselect" allow-clear v-model="checkboxList" placeholder="可多选" multiple :limit="6">
                    <a-option v-for="item in weekList" :key="item.key" :label="item.value" :value="item.key" />
                </a-select>
            </a-radio>
        </a-form-item>

    </a-form>
</template>

<script setup>
const emit = defineEmits(['update'])
const props = defineProps({
    cron: {
        type: Object,
        default: {
            second: "*",
            min: "*",
            hour: "*",
            day: "*",
            month: "*",
            week: "?",
            year: ""
        }
    },
    check: {
        type: Function,
        default: () => {
        }
    }
})
const radioValue = ref(2)
const cycle01 = ref(2)
const cycle02 = ref(3)
const average01 = ref(1)
const average02 = ref(2)
const weekday = ref(2)
const checkboxList = ref([])
const checkCopy = ref([2])
const weekList = ref([
    {key: 1, value: '星期日'},
    {key: 2, value: '星期一'},
    {key: 3, value: '星期二'},
    {key: 4, value: '星期三'},
    {key: 5, value: '星期四'},
    {key: 6, value: '星期五'},
    {key: 7, value: '星期六'}
])
const cycleTotal = computed(() => {
    cycle01.value = props.check(cycle01.value, 1, 6)
    cycle02.value = props.check(cycle02.value, cycle01.value + 1, 7)
    return cycle01.value + '-' + cycle02.value
})
const averageTotal = computed(() => {
    average01.value = props.check(average01.value, 1, 4)
    average02.value = props.check(average02.value, 1, 7)
    return average02.value + '#' + average01.value
})
const weekdayTotal = computed(() => {
    weekday.value = props.check(weekday.value, 1, 7)
    return weekday.value + 'L'
})
const checkboxString = computed(() => {
    return checkboxList.value.join(',')
})
watch(() => props.cron.week, value => changeRadioValue(value))
watch([radioValue, cycleTotal, averageTotal, weekdayTotal, checkboxString], () => onRadioChange())
function changeRadioValue(value) {
    if (value === "*") {
        radioValue.value = 1
    } else if (value === "?") {
        radioValue.value = 2
    } else if (value.indexOf("-") > -1) {
        const indexArr = value.split('-')
        cycle01.value = Number(indexArr[0])
        cycle02.value = Number(indexArr[1])
        radioValue.value = 3
    } else if (value.indexOf("#") > -1) {
        const indexArr = value.split('#')
        average01.value = Number(indexArr[1])
        average02.value = Number(indexArr[0])
        radioValue.value = 4
    } else if (value.indexOf("L") > -1) {
        const indexArr = value.split("L")
        weekday.value = Number(indexArr[0])
        radioValue.value = 5
    } else {
        checkboxList.value = [...new Set(value.split(',').map(item => Number(item)))]
        radioValue.value = 6
    }
}
function onRadioChange() {
    if (radioValue.value === 2 && props.cron.day === '?') {
        emit('update', 'day', '*', 'week')
    }
    if (radioValue.value !== 2 && props.cron.day !== '?') {
        emit('update', 'day', '?', 'week')
    }
    switch (radioValue.value) {
        case 1:
            emit('update', 'week', '*', 'week')
            break
        case 2:
            emit('update', 'week', '?', 'week')
            break
        case 3:
            emit('update', 'week', cycleTotal.value, 'week')
            break
        case 4:
            emit('update', 'week', averageTotal.value, 'week')
            break
        case 5:
            emit('update', 'week', weekdayTotal.value, 'week')
            break
        case 6:
            if (checkboxList.value.length === 0) {
                checkboxList.value.push(checkCopy.value[0])
            } else {
                checkCopy.value = checkboxList.value
            }
            emit('update', 'week', checkboxString.value, 'week')
            break
    }
}
</script>

<style lang="scss" scoped>
.arco-input-number, .arco-select {
    margin: 0 0.5rem;
}
.arco-select {
    width: 8rem;
}
.arco-select.multiselect {
    width: 17.8rem;
}
</style>
