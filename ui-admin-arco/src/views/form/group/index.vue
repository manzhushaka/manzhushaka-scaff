<template>
  <div class="container">
    <a-form ref="formRef" layout="vertical" :model="formData">
      <a-space direction="vertical" :size="16">
        <a-card class="general-card">
          <template #title>
            {{ $t('groupForm.title.video') }}
          </template>
          <a-row :gutter="80">
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.video.mode')"
                field="video.mode"
              >
                <a-select
                  v-model="formData.video.mode"
                  :placeholder="$t('groupForm.placeholder.video.mode')"
                >
                  <a-option value="custom">自定义</a-option>
                  <a-option value="mode1">模式1</a-option>
                  <a-option value="mode2">模式2</a-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.video.acquisition.resolution')"
                field="video.acquisition.resolution"
              >
                <a-select
                  v-model="formData.video.acquisition.resolution"
                  :placeholder="
                    $t('groupForm.placeholder.video.acquisition.resolution')
                  "
                >
                  <a-option value="resolution1">分辨率1</a-option>
                  <a-option value="resolution2">分辨率2</a-option>
                  <a-option value="resolution3">分辨率3</a-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.video.acquisition.frameRate')"
                field="video.acquisition.frameRate"
              >
                <a-input
                  v-model="formData.video.acquisition.frameRate"
                  :placeholder="
                    $t('groupForm.placeholder.video.acquisition.frameRate')
                  "
                >
                  <template #append> fps </template>
                </a-input>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="80">
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.video.encoding.resolution')"
                field="video.encoding.resolution"
              >
                <a-select
                  v-model="formData.video.encoding.resolution"
                  :placeholder="
                    $t('groupForm.placeholder.video.encoding.resolution')
                  "
                >
                  <a-option value="resolution1">分辨率1</a-option>
                  <a-option value="resolution2">分辨率2</a-option>
                  <a-option value="resolution3">分辨率3</a-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.video.encoding.rate.min')"
                field="video.encoding.rate.min"
              >
                <a-input
                  v-model="formData.video.encoding.rate.min"
                  :placeholder="
                    $t('groupForm.placeholder.video.encoding.rate.min')
                  "
                  add-after="bps"
                >
                  <template #append> bps </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.video.encoding.rate.max')"
                field="video.encoding.rate.max"
              >
                <a-input
                  v-model="formData.video.encoding.rate.max"
                  :placeholder="
                    $t('groupForm.placeholder.video.encoding.rate.max')
                  "
                >
                  <template #append> bps </template>
                </a-input>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="80">
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.video.encoding.rate.default')"
                field="video.encoding.rate.default"
              >
                <a-input
                  v-model="formData.video.encoding.rate.default"
                  :placeholder="
                    $t('groupForm.placeholder.video.encoding.rate.default')
                  "
                >
                  <template #append> bps </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.video.encoding.frameRate')"
                field="video.encoding.frameRate"
              >
                <a-input
                  v-model="formData.video.encoding.frameRate"
                  :placeholder="
                    $t('groupForm.placeholder.video.encoding.frameRate')
                  "
                >
                  <template #append> fps </template>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.video.encoding.profile')"
                field="video.encoding.profile"
              >
                <a-input
                  v-model="formData.video.encoding.profile"
                  :placeholder="
                    $t('groupForm.placeholder.video.encoding.profile')
                  "
                >
                  <template #append> bps </template>
                </a-input>
              </a-form-item>
            </a-col>
          </a-row>
        </a-card>
        <a-card class="general-card">
          <template #title>
            {{ $t('groupForm.title.audio') }}
          </template>
          <a-row :gutter="80">
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.audio.mode')"
                field="audio.mode"
              >
                <a-select
                  v-model="formData.audio.mode"
                  :placeholder="$t('groupForm.placeholder.audio.mode')"
                >
                  <a-option value="custom">自定义</a-option>
                  <a-option value="mode1">模式1</a-option>
                  <a-option value="mode2">模式2</a-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.audio.acquisition.channels')"
                field="audio.acquisition.channels"
              >
                <a-select
                  v-model="formData.audio.acquisition.channels"
                  :placeholder="
                    $t('groupForm.placeholder.audio.acquisition.channels')
                  "
                >
                  <a-option value="1">1</a-option>
                  <a-option value="2">2</a-option>
                  <a-option value="3">3</a-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.audio.encoding.channels')"
                field="audio.encoding.channels"
              >
                <a-input
                  v-model="formData.audio.encoding.channels"
                  :placeholder="
                    $t('groupForm.placeholder.audio.encoding.channels')
                  "
                >
                  <template #append> bps </template>
                </a-input>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="80">
            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.audio.encoding.rate')"
                field="audio.encoding.rate"
              >
                <a-input
                  v-model="formData.audio.encoding.rate"
                  :placeholder="$t('groupForm.placeholder.audio.encoding.rate')"
                >
                  <template #append> bps </template>
                </a-input>
              </a-form-item>
            </a-col>

            <a-col :span="8">
              <a-form-item
                :label="$t('groupForm.form.label.audio.encoding.profile')"
                field="audio.encoding.profile"
              >
                <a-input
                  v-model="formData.audio.encoding.profile"
                  :placeholder="
                    $t('groupForm.placeholder.audio.encoding.profile')
                  "
                >
                  <template #append> fps </template>
                </a-input>
              </a-form-item>
            </a-col>
          </a-row>
        </a-card>
        <a-card class="general-card" :bordered="false">
          <template #title>
            {{ $t('groupForm.title.description') }}
          </template>
          <a-form-item
            :label="$t('groupForm.form.label.parameterDescription')"
            field="description"
          >
            <a-textarea
              v-model="formData.description"
              :placeholder="$t('groupForm.placeholder.description')"
            />
          </a-form-item>
        </a-card>
      </a-space>
      <div class="actions">
        <a-space>
          <a-button @click="resetForm">
            {{ $t('groupForm.reset') }}
          </a-button>
          <a-button type="primary" :loading="loading" @click="onSubmitClick">
            {{ $t('groupForm.submit') }}
          </a-button>
        </a-space>
      </div>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import useLoading from '@/hooks/loading';

  const formData = ref({
    video: {
      mode: '',
      acquisition: { resolution: '', frameRate: '' },
      encoding: {
        resolution: '',
        rate: { min: '', max: '', default: '' },
        frameRate: '',
        profile: '',
      },
    },
    audio: {
      mode: '',
      acquisition: { channels: '' },
      encoding: { channels: '', rate: '', profile: '' },
    },
    description: '',
  });
  const formRef = ref<FormInstance>();
  const { loading, setLoading } = useLoading();
  /** 校验分组表单并反馈当前表单状态。 */
  async function onSubmitClick() {
    if (!formRef.value) return;

    setLoading(true);
    try {
      const errors = await formRef.value.validate();
      if (errors) return;

      Message.success('表单校验通过');
    } finally {
      setLoading(false);
    }
  }

  /** 重置所有分组表单字段。 */
  function resetForm() {
    formRef.value?.resetFields();
  }
</script>

<script lang="ts">
  export default {
    name: 'Group',
  };
</script>

<style scoped lang="less">
  .container {
    padding: 20px 20px 40px;
    overflow: hidden;
  }

  .actions {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    height: 60px;
    padding: 14px 20px 14px 0;
    background: var(--color-bg-2);
    text-align: right;
  }

  @media (max-width: 640px) {
    .container {
      padding: 12px 12px 80px;
    }

    .container :deep(.arco-col-8) {
      flex: 0 0 100%;
      width: 100%;
      max-width: 100%;
    }

    .actions {
      padding: 14px 12px;
    }
  }
</style>
