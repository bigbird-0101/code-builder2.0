(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["pages-intermediate-mescroll-body-part"],{"0f2b":function(t,o,e){var i=e("24fb");o=i(!1),o.push([t.i,'@charset "UTF-8";\n/* 顶部 fixed定位*/.top-warp[data-v-7505f610]{z-index:200;position:fixed;top:var(--window-top);left:0;width:100%;height:%?88?%;padding-top:%?10?%;font-size:%?28?%;text-align:center;background-color:#cfe0da}\n/* 左边 fixed定位*/.left-warp[data-v-7505f610]{z-index:100;position:fixed;top:var(--window-top);left:0;bottom:%?100?%;width:%?180?%;padding-top:%?88?%;background-color:#eee}.left-warp .tab[data-v-7505f610]{font-size:%?28?%;padding:%?28?%}.left-warp .tab.active[data-v-7505f610]{background-color:#fff}.mescroll-body[data-v-7505f610],[data-v-7505f610] .mescroll-body{padding-left:%?180?%}\n/* 底部 fixed定位*/.bottom-warp[data-v-7505f610]{z-index:200;position:fixed;left:0;bottom:0;width:100%;height:%?100?%;line-height:%?100?%;text-align:center;background-color:#ff6990}',""]),t.exports=o},"173a":function(t,o,e){var i=e("24fb");o=i(!1),o.push([t.i,".good-list[data-v-741419e6]{background-color:#fff}.good-list .good-li[data-v-741419e6]{display:flex;align-items:center;padding:%?20?%;border-bottom:%?1?% solid #eee}.good-list .good-li .good-img[data-v-741419e6]{width:%?160?%;height:%?160?%;margin-right:%?20?%}.good-list .good-li .flex-item[data-v-741419e6]{flex:1}.good-list .good-li .flex-item .good-name[data-v-741419e6]{font-size:%?26?%;line-height:%?40?%;height:%?80?%;margin-bottom:%?20?%;overflow:hidden}.good-list .good-li .flex-item .good-price[data-v-741419e6]{font-size:%?26?%;color:red}.good-list .good-li .flex-item .good-sold[data-v-741419e6]{font-size:%?24?%;margin-left:%?16?%;color:grey}",""]),t.exports=o},"2f9d":function(t,o,e){"use strict";e.r(o);var i=e("52d5"),a=e("efd2");for(var n in a)"default"!==n&&function(t){e.d(o,t,(function(){return a[t]}))}(n);e("d214");var d,s=e("f0c5"),r=Object(s["a"])(a["default"],i["b"],i["c"],!1,null,"741419e6",null,!1,i["a"],d);o["default"]=r.exports},4497:function(t,o,e){var i=e("0f2b");"string"===typeof i&&(i=[[t.i,i,""]]),i.locals&&(t.exports=i.locals);var a=e("4f06").default;a("30b49a23",i,!0,{sourceMap:!1,shadowMode:!1})},"52d5":function(t,o,e){"use strict";var i;e.d(o,"b",(function(){return a})),e.d(o,"c",(function(){return n})),e.d(o,"a",(function(){return i}));var a=function(){var t=this,o=t.$createElement,e=t._self._c||o;return e("v-uni-view",{staticClass:"good-list"},t._l(t.list,(function(o){return e("v-uni-view",{key:o.id,staticClass:"good-li",attrs:{id:"good"+o.id}},[e("v-uni-image",{staticClass:"good-img",attrs:{src:o.goodImg,mode:"widthFix"}}),e("v-uni-view",{staticClass:"flex-item"},[e("v-uni-view",{staticClass:"good-name"},[t._v(t._s(o.goodName))]),e("v-uni-text",{staticClass:"good-price"},[t._v(t._s(o.goodPrice)+" 元")]),e("v-uni-text",{staticClass:"good-sold"},[t._v("已售"+t._s(o.goodSold)+"件")])],1)],1)})),1)},n=[]},7130:function(t,o,e){"use strict";e.r(o);var i=e("f8df"),a=e.n(i);for(var n in i)"default"!==n&&function(t){e.d(o,t,(function(){return i[t]}))}(n);o["default"]=a.a},8803:function(t,o,e){var i=e("173a");"string"===typeof i&&(i=[[t.i,i,""]]),i.locals&&(t.exports=i.locals);var a=e("4f06").default;a("59c469df",i,!0,{sourceMap:!1,shadowMode:!1})},a013:function(t,o,e){"use strict";Object.defineProperty(o,"__esModule",{value:!0}),o.default=void 0;var i={props:{list:{type:Array,default:function(){return[]}}}};o.default=i},a073:function(t,o,e){"use strict";e.d(o,"b",(function(){return a})),e.d(o,"c",(function(){return n})),e.d(o,"a",(function(){return i}));var i={mescrollBody:e("0ead").default,goodList:e("2f9d").default},a=function(){var t=this,o=t.$createElement,e=t._self._c||o;return e("v-uni-view",[e("v-uni-view",{staticClass:"top-warp"},[e("v-uni-view",[t._v("顶部区域")]),e("v-uni-view",{staticStyle:{"font-size":"24rpx"}},[t._v('mescroll-body 通过fixed定位其他元素,实现"局部区域滚动"')])],1),e("v-uni-scroll-view",{staticClass:"left-warp",attrs:{"scroll-y":!0}},t._l(t.tabs,(function(o,i){return e("v-uni-view",{key:i,staticClass:"tab",class:{active:i==t.tabIndex},on:{click:function(o){arguments[0]=o=t.$handleEvent(o),t.tabChange(i)}}},[t._v(t._s(o))])})),1),e("mescroll-body",{ref:"mescrollRef",attrs:{top:"88",bottom:"100"},on:{init:function(o){arguments[0]=o=t.$handleEvent(o),t.mescrollInit.apply(void 0,arguments)},down:function(o){arguments[0]=o=t.$handleEvent(o),t.downCallback.apply(void 0,arguments)},up:function(o){arguments[0]=o=t.$handleEvent(o),t.upCallback.apply(void 0,arguments)}}},[e("good-list",{attrs:{list:t.goods}})],1),e("v-uni-view",{staticClass:"bottom-warp"},[t._v("底部区域")])],1)},n=[]},ab8d:function(t,o,e){"use strict";e.r(o);var i=e("a073"),a=e("7130");for(var n in a)"default"!==n&&function(t){e.d(o,t,(function(){return a[t]}))}(n);e("ae77");var d,s=e("f0c5"),r=Object(s["a"])(a["default"],i["b"],i["c"],!1,null,"7505f610",null,!1,i["a"],d);o["default"]=r.exports},ae77:function(t,o,e){"use strict";var i=e("4497"),a=e.n(i);a.a},d214:function(t,o,e){"use strict";var i=e("8803"),a=e.n(i);a.a},efd2:function(t,o,e){"use strict";e.r(o);var i=e("a013"),a=e.n(i);for(var n in i)"default"!==n&&function(t){e.d(o,t,(function(){return i[t]}))}(n);o["default"]=a.a},f8df:function(t,o,e){"use strict";var i=e("4ea4");e("99af"),Object.defineProperty(o,"__esModule",{value:!0}),o.default=void 0;var a=i(e("fabb")),n=e("d63f"),d={mixins:[a.default],data:function(){return{goods:[],tabs:[],tabIndex:0}},methods:{upCallback:function(t){var o=this;if(0!=this.tabs.length){var e=this.tabs[this.tabIndex];(0,n.apiGoods)(t.num,t.size,e).then((function(e){o.mescroll.endSuccess(e.list.length),1==t.num&&(o.goods=[]),o.goods=o.goods.concat(e.list)})).catch((function(){o.mescroll.endErr()}))}else(0,n.apiGetTabs)().then((function(t){o.tabs=t,o.mescroll.resetUpScroll()})).catch((function(){o.mescroll.endErr()}))},tabChange:function(t){this.tabIndex!=t&&(this.tabIndex=t,this.goods=[],this.mescroll.resetUpScroll())}}};o.default=d}}]);