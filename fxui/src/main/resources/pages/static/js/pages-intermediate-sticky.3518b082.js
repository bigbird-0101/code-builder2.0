(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["pages-intermediate-sticky"],{"306f":function(t,e,i){var n=i("bf75");"string"===typeof n&&(n=[[t.i,n,""]]),n.locals&&(t.exports=n.locals);var a=i("4f06").default;a("6ef1ef34",n,!0,{sourceMap:!1,shadowMode:!1})},6207:function(t,e,i){"use strict";var n=i("306f"),a=i.n(n);a.a},"803c":function(t,e,i){"use strict";var n=i("4ea4");i("99af"),Object.defineProperty(e,"__esModule",{value:!0}),e.default=void 0;var a=n(i("fabb")),o=i("d63f"),s={mixins:[a.default],data:function(){return{goods:[],tabs:[{name:"全部",type:"xx"},{name:"奶粉",type:"xx"},{name:"图书",type:"xx"}],tabIndex:0}},methods:{downCallback:function(){this.mescroll.resetUpScroll()},upCallback:function(t){var e=this,i=this.tabs[this.tabIndex],n=i.name;(0,o.apiGoods)(t.num,t.size,n).then((function(i){1==t.num&&(e.goods=[]),e.goods=e.goods.concat(i.list),e.mescroll.endSuccess(i.list.length)})).catch((function(){e.mescroll.endErr()}))},tabChange:function(){this.goods=[],this.mescroll.resetUpScroll()}}};e.default=s},"8a2e":function(t,e,i){"use strict";i.r(e);var n=i("c2bf"),a=i("ec4b");for(var o in a)"default"!==o&&function(t){i.d(e,t,(function(){return a[t]}))}(o);i("6207");var s,c=i("f0c5"),r=Object(c["a"])(a["default"],n["b"],n["c"],!1,null,"9a75c1c2",null,!1,n["a"],s);e["default"]=r.exports},bf75:function(t,e,i){var n=i("24fb");e=n(!1),e.push([t.i,'@charset "UTF-8";\n/*\nsticky生效条件：\n1、父元素不能overflow:hidden或者overflow:auto属性。(mescroll-body设置:sticky="true"即可, mescroll-uni本身没有设置overflow)\n2、必须指定top、bottom、left、right4个值之一，否则只会处于相对定位\n3、父元素的高度不能低于sticky元素的高度\n4、sticky元素仅在其父元素内生效,所以父元素必须是 mescroll\n*/.sticky-tabs[data-v-9a75c1c2]{z-index:990;position:-webkit-sticky;position:sticky;top:var(--window-top);background-color:#fff}.mescroll-uni .sticky-tabs[data-v-9a75c1c2],[data-v-9a75c1c2] .mescroll-uni .sticky-tabs{top:0}.demo-tip[data-v-9a75c1c2]{padding:%?18?%;font-size:%?24?%;text-align:center}',""]),t.exports=e},c2bf:function(t,e,i){"use strict";i.d(e,"b",(function(){return a})),i.d(e,"c",(function(){return o})),i.d(e,"a",(function(){return n}));var n={mescrollBody:i("0ead").default,meTabs:i("1db9").default,goodList:i("2f9d").default},a=function(){var t=this,e=t.$createElement,i=t._self._c||e;return i("v-uni-view",[i("mescroll-body",{ref:"mescrollRef",attrs:{sticky:!0},on:{init:function(e){arguments[0]=e=t.$handleEvent(e),t.mescrollInit.apply(void 0,arguments)},down:function(e){arguments[0]=e=t.$handleEvent(e),t.downCallback.apply(void 0,arguments)},up:function(e){arguments[0]=e=t.$handleEvent(e),t.upCallback.apply(void 0,arguments)}}},[i("v-uni-swiper",{staticStyle:{"min-height":"300rpx"},attrs:{autoplay:"true",interval:"3000",duration:"300",circular:"true"}},[i("v-uni-swiper-item",[i("v-uni-image",{staticStyle:{width:"100%",height:"auto"},attrs:{src:"https://www.mescroll.com/img/swiper1.jpg",mode:"widthFix"}})],1),i("v-uni-swiper-item",[i("v-uni-image",{staticStyle:{width:"100%",height:"auto"},attrs:{src:"https://www.mescroll.com/img/swiper2.jpg",mode:"widthFix"}})],1)],1),i("v-uni-view",{staticClass:"demo-tip"},[i("v-uni-view",[t._v("每次切换菜单,都刷新列表数据")]),i("v-uni-view",[t._v("吸顶通过给菜单加position:sticky实现, 用法简单")]),i("v-uni-view",[t._v("小程序和微信h5端: 低端机sticky也可生效, 可放心使用")]),i("v-uni-view",[t._v("APP端: 仅部分低端机无效,若要兼容则参考sticky-scroll")])],1),i("v-uni-view",{staticClass:"sticky-tabs"},[i("me-tabs",{attrs:{tabs:t.tabs},on:{change:function(e){arguments[0]=e=t.$handleEvent(e),t.tabChange.apply(void 0,arguments)}},model:{value:t.tabIndex,callback:function(e){t.tabIndex=e},expression:"tabIndex"}})],1),i("good-list",{attrs:{list:t.goods}})],1)],1)},o=[]},ec4b:function(t,e,i){"use strict";i.r(e);var n=i("803c"),a=i.n(n);for(var o in n)"default"!==o&&function(t){i.d(e,t,(function(){return n[t]}))}(o);e["default"]=a.a}}]);