<template>
  <div ref="tableContainer" class="auto-height-table-container">
    <a-table
      ref="tableRef"
      v-bind="$attrs"
      :scroll="processedScroll"
      :pagination="processedPagination"
      v-on="$attrs"
    >
      <template v-for="(_, slot) in $slots" #[slot]="scope" :key="slot">
        <slot :name="slot" v-bind="scope" />
      </template>
    </a-table>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch, nextTick, useAttrs } from 'vue';
import { Table } from 'ant-design-vue';


export default {
  name: 'AutoHeightTable',
  components: {
    ATable: Table
  },
  inheritAttrs: false,
  props: {
    scroll: {
      type: Object,
      default: () => ({})
    },
    // 预留空间，避免滚动条问题
    reservedSpace: {
      type: Number,
      default: 70
    }
  },
  setup(props) {
    const attrs = useAttrs();
    const tableContainer = ref(null);
    const tableRef = ref(null);
    const containerHeight = ref(0);
    const resizeObserver = ref(null);

    // 计算处理后的分页配置，包含中文本地化
    const processedPagination = computed(() => {
      const pagination = attrs.pagination;
      
      // 如果存在分页配置，自动添加中文本地化
      if (pagination) {
        return {
          ...pagination,
          locale: {
            items_per_page: '条/页',
            jump_to: '跳至',
            jump_to_confirm: '确定',
            page: '页',
            prev_page: '上一页',
            next_page: '下一页',
            prev_5: '向前 5 页',
            next_5: '向后 5 页',
            prev_3: '向前 3 页',
            next_3: '向后 3 页',
            ...pagination.locale // 允许外部覆盖
          }
        };
      }
      
      return pagination;
    });

    // 计算处理后的滚动属性
    const processedScroll = computed(() => {
      const scrollConfig = { ...props.scroll };
      
      // 如果 y 设置为 'max-content'，则计算容器可用高度
      if (scrollConfig.y === 'max-content') {
        // 减去预留空间
        scrollConfig.y = containerHeight.value > 0 ? Math.max(containerHeight.value - props.reservedSpace, 200) : 400;
      }
      
      return scrollConfig;
    });

    // 更新容器高度
    const updateContainerHeight = () => {
      if (tableContainer.value) {
        // 获取容器的可用高度
        const containerRect = tableContainer.value.getBoundingClientRect();
        const viewportHeight = window.innerHeight;
        
        // 计算从容器顶部到视口底部的高度
        const availableHeight = viewportHeight - containerRect.top - props.reservedSpace;
        
        // 确保最小高度
        containerHeight.value = Math.max(availableHeight, 200);
      }
    };

    // 初始化 ResizeObserver
    const initResizeObserver = () => {
      if (window.ResizeObserver && tableContainer.value) {
        resizeObserver.value = new ResizeObserver(() => {
          updateContainerHeight();
        });
        resizeObserver.value.observe(tableContainer.value);
      }
    };

    // 清理 ResizeObserver
    const cleanupResizeObserver = () => {
      if (resizeObserver.value && tableContainer.value) {
        resizeObserver.value.unobserve(tableContainer.value);
        resizeObserver.value = null;
      }
    };

    // 监听窗口大小变化
    const handleResize = () => {
      updateContainerHeight();
    };

    // 组件挂载时
    onMounted(() => {
      nextTick(() => {
        updateContainerHeight();
        initResizeObserver();
        window.addEventListener('resize', handleResize);
      });
    });

    // 组件卸载时
    onUnmounted(() => {
      cleanupResizeObserver();
      window.removeEventListener('resize', handleResize);
    });

    // 监听 scroll 属性变化
    watch(
      () => props.scroll,
      () => {
        nextTick(() => {
          updateContainerHeight();
        });
      },
      { deep: true }
    );

    // 暴露方法给父组件
    const refreshHeight = () => {
      updateContainerHeight();
    };

    return {
      tableContainer,
      tableRef,
      processedScroll,
      processedPagination,
      refreshHeight
    };
  }
};
</script>

<style scoped>
.auto-height-table-container {
  width: 100%;
  height: 100%;
}
</style>