/* ============================================================
   超市销售分析平台 - 公共脚本
   ============================================================ */

/** 图表统一配色 */
const PALETTE = ['#4f7cff', '#2fbf8f', '#ff9f43', '#a06cf0', '#28c3d4',
                 '#f05b9d', '#ffd84d', '#7b93c9', '#52c4a8', '#f0826f',
                 '#8fbf3f', '#c986e0'];

const ECHARTS = {};

/** 当前登录用户（checkAuth 成功后填充） */
let CURRENT_USER = null;

/**
 * 请求后端接口（JSON）
 * 401（登录过期）时自动跳转登录页
 */
async function api(url, options) {
    const resp = await fetch(url, options);
    if (resp.status === 401) {
        location.href = '/login.html';
        throw new Error('未登录或登录已过期，请重新登录');
    }
    if (!resp.ok) {
        let msg = '请求失败：HTTP ' + resp.status;
        try {
            const body = await resp.json();
            if (body && body.message) msg = body.message;
        } catch (e) { /* ignore */ }
        throw new Error(msg);
    }
    return resp.json();
}

/** POST JSON 请求 */
async function postApi(url, body) {
    return api(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
}

/**
 * 初始化图表并渲染，返回 echarts 实例；自动附带"导出图片"工具栏
 */
function initChart(el, option) {
    if (!el) return null;
    option.toolbox = Object.assign({
        right: 8,
        top: 0,
        itemSize: 13,
        feature: {
            saveAsImage: {
                title: '导出图片',
                name: (document.title || 'chart').replace(/ - .*/, ''),
                backgroundColor: '#fff'
            }
        }
    }, option.toolbox || {});
    const inst = echarts.init(el);
    inst.setOption(option);
    ECHARTS[el.id] = inst;
    return inst;
}

/** 在容器内显示"暂无数据"遮罩 */
function showEmptyChart(el, msg) {
    if (!el) return;
    const box = el.parentElement;
    if (!box.querySelector('.empty-overlay')) {
        const div = document.createElement('div');
        div.className = 'empty-overlay';
        div.innerHTML = '<span class="icon">📭</span><span>' + (msg || '暂无数据') + '</span>';
        box.appendChild(div);
    }
}

/** 移除"暂无数据"遮罩 */
function hideEmptyChart(el) {
    if (!el) return;
    const box = el.parentElement;
    const ov = box.querySelector('.empty-overlay');
    if (ov) ov.remove();
}

/** 空数据横幅 */
function showEmptyBanner() {
    const banner = document.createElement('div');
    banner.className = 'empty-banner';
    banner.innerHTML = '⚠️ <b>MySQL 结果表还没有数据。</b>请先在虚拟机中完成 Hive 分析并导出：'
        + '① 在 Hive 中执行 <code>hive/hive-analysis.sql</code> 产出 16 张结果表；'
        + '② 执行 <code>hive/sqoop-export.sh</code> 将结果表导出到本地 MySQL。'
        + '完成后回到本页刷新即可看到分析图表。';
    document.querySelector('.content').prepend(banner);
}

/** 检查结果表是否有数据；无数据时自动显示横幅 */
async function ensureData() {
    try {
        const kpi = await api('/api/dashboard/summary');
        const has = kpi && Number(kpi.totalOrders) > 0;
        if (!has) showEmptyBanner();
        return has;
    } catch (e) {
        toast(e.message, 'error');
        return false;
    }
}

/** 判断列表是否为空（用于图表） */
function isEmpty(list) {
    return !list || list.length === 0;
}

/** 金额格式化：自动转万/亿，保留两位小数 */
function fmtMoney(v) {
    if (v === null || v === undefined || v === '') return '¥0';
    const n = Number(v);
    if (isNaN(n)) return '¥0';
    const abs = Math.abs(n);
    if (abs >= 1e8) return '¥' + (n / 1e8).toFixed(2) + '亿';
    if (abs >= 1e4) return '¥' + (n / 1e4).toFixed(1) + '万';
    return '¥' + n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

/** 整数千分位 */
function fmtInt(v) {
    if (v === null || v === undefined) return '0';
    return Number(v).toLocaleString('zh-CN');
}

/** 百分比 */
function fmtPct(v) {
    if (v === null || v === undefined) return '-';
    return Number(v).toFixed(2) + '%';
}

/** HTML 转义（防注入） */
function esc(s) {
    return String(s === null || s === undefined ? '' : s)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

/** 右上角消息提示 */
function toast(msg, type) {
    const div = document.createElement('div');
    div.className = 'toast ' + (type || 'info');
    div.textContent = msg;
    document.body.appendChild(div);
    setTimeout(() => div.remove(), 3500);
}

/** ECharts 通用 tooltip 样式 */
function moneyTooltip() {
    return {
        trigger: 'axis',
        backgroundColor: '#fff',
        borderColor: '#e8ecf4',
        textStyle: { color: '#2b3445', fontSize: 12.5 },
        valueFormatter: null
    };
}

/** 月份名显示 */
function monthLabel(m) {
    return m.replace('-', '年') + '月';
}

/** 顶部用户信息渲染 */
function renderUser() {
    const box = document.getElementById('userBox');
    if (box && CURRENT_USER) {
        box.textContent = '👤 ' + (CURRENT_USER.nickname || CURRENT_USER.username);
        box.title = '账号：' + CURRENT_USER.username;
    }
}

/** 退出登录 */
async function logout() {
    try { await postApi('/api/auth/logout'); } catch (e) { /* ignore */ }
    location.href = '/login.html';
}

/** 登录检查：业务页面（带 data-page 属性）加载时校验会话并渲染用户信息 */
async function checkAuth() {
    if (!document.body.hasAttribute('data-page')) return true;
    try {
        CURRENT_USER = await api('/api/auth/me');
        renderUser();
        return true;
    } catch (e) {
        return false; // api() 已重定向到登录页
    }
}

// 侧边栏高亮
(function () {
    const page = document.body.getAttribute('data-page');
    document.querySelectorAll('.sidebar nav a[data-nav]').forEach(a => {
        if (a.getAttribute('data-nav') === page) a.classList.add('active');
    });
    // 顶部日期
    const today = document.getElementById('today');
    if (today) {
        const d = new Date();
        const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()];
        today.textContent = d.getFullYear() + '年' + (d.getMonth() + 1) + '月' + d.getDate()
            + '日 星期' + week;
    }
})();

// 登录校验 + 用户信息
checkAuth();

// 窗口缩放时自适应
window.addEventListener('resize', () => {
    Object.values(ECHARTS).forEach(c => c.resize());
});
