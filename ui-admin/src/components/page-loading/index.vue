<template>
  <main class="page-loading" aria-busy="true" aria-label="页面加载中">
    <section class="page-loading__filter">
      <a-skeleton :animation="true">
        <a-skeleton-line :rows="2" :widths="['72%', '48%']" />
      </a-skeleton>
    </section>
    <section class="page-loading__workspace">
      <div class="page-loading__toolbar">
        <a-skeleton :animation="true">
          <a-skeleton-line :rows="1" :widths="['28%']" />
        </a-skeleton>
      </div>
      <div class="page-loading__content">
        <a-skeleton :animation="true">
          <a-skeleton-line
            :rows="7"
            :widths="['100%', '94%', '97%', '91%', '96%', '93%', '98%']"
          />
        </a-skeleton>
      </div>
    </section>
  </main>
</template>

<style scoped lang="less">
  .page-loading {
    min-height: 100%;
    padding: 16px 20px 20px;
    background: var(--ui-bg-content, var(--color-fill-2));
  }

  .page-loading__filter,
  .page-loading__workspace {
    border: 1px solid var(--color-border-2);
    border-radius: 6px;
    background: var(--ui-bg-panel, var(--color-bg-2));
  }

  .page-loading__filter {
    padding: 18px 20px;
    margin-bottom: 16px;
  }

  .page-loading__workspace {
    overflow: hidden;
  }

  .page-loading__toolbar {
    padding: 16px 20px;
    border-bottom: 1px solid var(--color-border-2);
  }

  .page-loading__content {
    min-height: 320px;
    padding: 20px;
  }

  @media (max-width: 640px) {
    .page-loading {
      padding: 12px;
    }

    .page-loading__filter {
      padding: 14px;
      margin-bottom: 12px;
    }

    .page-loading__toolbar,
    .page-loading__content {
      padding: 14px;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    .page-loading :deep(.arco-skeleton-line-row) {
      animation: none;
    }
  }
</style>
