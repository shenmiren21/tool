
(function ($) {
    $.extend({
            _: {
                id: function (id) {
                    return document.getElementById(id);
                },
                name: function (name) {
                    return document.getElementsByName(name);
                }
            },
            u: {
                    _host: function () {
                        var l = window.location;
                        if (l.port) {
                            return l.protocol + "//" + l.hostname + ":" + l.port;
                        } else {
                            return l.protocol + "//" + l.hostname;
                        }
                    }
                },
            file:{
                    _view:function (ur,oldSegment,newSegment){
                        var url = u._host()+ur;
                        // 通用路径替换逻辑
                        function replacePathSegment(url, oldSegment, newSegment) {
                            return url.replace(new RegExp(oldSegment, 'g'), newSegment);
                        }
                        if (oldSegment && newSegment){
                            // 替换路径段
                            url = replacePathSegment(url, oldSegment, newSegment);
                        }
                        // 判断文件扩展名
                        const ext = url.split('.').pop().toLowerCase();

                        // 支持的预览类型
                        const officeExtensions = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx','pdf'];
                        const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp'];
                        const videoExtensions = ['mp4', 'avi', 'mov', 'mkv'];

                        let previewUrl;

                        if (officeExtensions.includes(ext)) {
                            // Office 文件预览
                            previewUrl = "https://view.officeapps.live.com/op/embed.aspx?src=" + encodeURIComponent(url);
                        } else if (imageExtensions.includes(ext)) {
                            // 图片预览
                            previewUrl = url; // 直接显示图片
                        } else if (videoExtensions.includes(ext)) {
                            // 视频预览
                            previewUrl = url; // 视频文件链接
                        } else {
                            // 其他文件类型下载
                            window.open(url, '_blank');
                            return;
                        }
                        // 打开预览窗口
                        const previewWindow = window.open(previewUrl, '_blank', 'width=800,height=600,scrollbars=yes,resizable=yes');
                        if (!previewWindow) {
                            alert("请允许弹出窗口以进行文件预览。");
                        }
                },

            },
            //ajax
            ajax: {
                // 提交数据
                submit: function (url, type, dataType, data, callback) {
                    var config = {
                        url: url,
                        type: type,
                        dataType: dataType,
                        data: data,
                        beforeSend: function () {
                            // $.modal.loading("正在处理中，请稍候...");
                        },
                        success: function (result) {
                            if (typeof callback == "function") {
                                callback(result);
                            }
                            // $.modal.closeLoading();
                        }
                    };
                    $.ajax(config)
                },
            },
            // 校验封装处理
            validate: {
                // 表单验证
                form: function (formId) {
                    var currentId = $.common.isEmpty(formId) ? $('form').attr('id') : formId;
                    return $("#" + currentId).validate().form();
                },
                // 重置表单验证（清除提示信息）
                reset: function (formId) {
                    var currentId = $.common.isEmpty(formId) ? $('form').attr('id') : formId;
                    return $("#" + currentId).validate().resetForm();
                }
            },
            // 通用方法封装处理
            common: {
                // 判断字符串是否为空
                isEmpty: function (value) {
                    if (value == null || this.trim(value) == "" || value == undefined || value == "undefined") {
                        return true;
                    }
                    return false;
                },
                // 判断一个字符串是否为非空串
                isNotEmpty: function (value) {
                    return !$.common.isEmpty(value);
                },
                // 如果值是空，则返回指定默认字符串，否则返回字符串本身
                nullToDefault: function (value, defaultValue) {
                    return $.common.isEmpty(value) ? defaultValue : value;
                },
                // 空对象转字符串
                nullToStr: function (value) {
                    if ($.common.isEmpty(value)) {
                        return "-";
                    }
                    return value;
                },
                // 是否显示数据 为空默认为显示
                visible: function (value) {
                    if ($.common.isEmpty(value) || value == true) {
                        return true;
                    }
                    return false;
                },
                // 空格截取
                trim: function (value) {
                    if (value == null) {
                        return "";
                    }
                    return value.toString().replace(/(^\s*)|(\s*$)|\r|\n/g, "");
                },
                // 比较两个字符串（大小写敏感）
                equals: function (str, that) {
                    return str == that;
                },
                // 比较两个字符串（大小写不敏感）
                equalsIgnoreCase: function (str, that) {
                    return String(str).toUpperCase() === String(that).toUpperCase();
                },
                // 将字符串按指定字符分割
                split: function (str, sep, maxLen) {
                    if ($.common.isEmpty(str)) {
                        return null;
                    }
                    var value = String(str).split(sep);
                    return maxLen ? value.slice(0, maxLen - 1) : value;
                },
                // 字符串格式化(%s )
                sprintf: function (str) {
                    var args = arguments, flag = true, i = 1;
                    str = str.replace(/%s/g, function () {
                        var arg = args[i++];
                        if (typeof arg === 'undefined') {
                            flag = false;
                            return '';
                        }
                        return arg == null ? '' : arg;
                    });
                    return flag ? str : '';
                },
                // 日期格式化 时间戳  -> yyyy-MM-dd HH-mm-ss
                dateFormat: function (date, format) {
                    var that = this;
                    if (that.isEmpty(date)) return "";
                    if (!date) return;
                    if (!format) format = "yyyy-MM-dd";
                    switch (typeof date) {
                        case "string":
                            date = new Date(date.replace(/-/g, "/"));
                            break;
                        case "number":
                            date = new Date(date);
                            break;
                    }
                    if (!(date instanceof Date)) return;
                    var dict = {
                        "yyyy": date.getFullYear(),
                        "M": date.getMonth() + 1,
                        "d": date.getDate(),
                        "H": date.getHours(),
                        "m": date.getMinutes(),
                        "s": date.getSeconds(),
                        "MM": ("" + (date.getMonth() + 101)).substr(1),
                        "dd": ("" + (date.getDate() + 100)).substr(1),
                        "HH": ("" + (date.getHours() + 100)).substr(1),
                        "mm": ("" + (date.getMinutes() + 100)).substr(1),
                        "ss": ("" + (date.getSeconds() + 100)).substr(1)
                    };
                    return format.replace(/(yyyy|MM?|dd?|HH?|ss?|mm?)/g,
                        function () {
                            return dict[arguments[0]];
                        });
                },
                // 获取节点数据，支持多层级访问
                getItemField: function (item, field) {
                    var value = item;
                    if (typeof field !== 'string' || item.hasOwnProperty(field)) {
                        return item[field];
                    }
                    var props = field.split('.');
                    for (var p in props) {
                        value = value && value[props[p]];
                    }
                    return value;
                },
                // 指定随机数返回
                random: function (min, max) {
                    return Math.floor((Math.random() * max) + min);
                },
                // 判断字符串是否是以start开头
                startWith: function (value, start) {
                    var reg = new RegExp("^" + start);
                    return reg.test(value)
                },
                // 判断字符串是否是以end结尾
                endWith: function (value, end) {
                    var reg = new RegExp(end + "$");
                    return reg.test(value)
                },
                // 数组去重
                uniqueFn: function (array) {
                    var result = [];
                    var hashObj = {};
                    for (var i = 0; i < array.length; i++) {
                        if (!hashObj[array[i]]) {
                            hashObj[array[i]] = true;
                            result.push(array[i]);
                        }
                    }
                    return result;
                },
                // 数组中的所有元素放入一个字符串
                join: function (array, separator) {
                    if ($.common.isEmpty(array)) {
                        return null;
                    }
                    return array.join(separator);
                },
                // 获取form下所有的字段并转换为json对象
                formToJSON: function (formId) {
                    var json = {};
                    $.each($("#" + formId).serializeArray(), function (i, field) {
                        if (json[field.name]) {
                            json[field.name] += ("," + field.value);
                        } else {
                            json[field.name] = field.value;
                        }
                    });
                    return json;
                },
                // 数据字典转下拉框
                dictToSelect: function (datas, value, name) {
                    var actions = [];
                    actions.push($.common.sprintf("<select class='form-control' name='%s'>", name));
                    $.each(datas, function (index, dict) {
                        actions.push($.common.sprintf("<option value='%s'", dict.dictValue));
                        if (dict.dictValue == ('' + value)) {
                            actions.push(' selected');
                        }
                        actions.push($.common.sprintf(">%s</option>", dict.dictLabel));
                    });
                    actions.push('</select>');
                    return actions.join('');
                },
                // 获取obj对象长度
                getLength: function (obj) {
                    var count = 0;
                    for (var i in obj) {
                        if (obj.hasOwnProperty(i)) {
                            count++;
                        }
                    }
                    return count;
                },
                // 判断移动端
                isMobile: function () {
                    return navigator.userAgent.match(/(Android|iPhone|SymbianOS|Windows Phone|iPad|iPod)/i);
                },
                // 数字正则表达式，只能为0-9数字
                numValid: function (text) {
                    var patten = new RegExp(/^[0-9]+$/);
                    return patten.test(text);
                },
                // 英文正则表达式，只能为a-z和A-Z字母
                enValid: function (text) {
                    var patten = new RegExp(/^[a-zA-Z]+$/);
                    return patten.test(text);
                },
                // 英文、数字正则表达式，必须包含（字母，数字）
                enNumValid: function (text) {
                    var patten = new RegExp(/^(?=.*[a-zA-Z]+)(?=.*[0-9]+)[a-zA-Z0-9]+$/);
                    return patten.test(text);
                },
                // 英文、数字、特殊字符正则表达式，必须包含（字母，数字，特殊字符!@#$%^&*()-=_+）
                charValid: function (text) {
                    var patten = new RegExp(/^(?=.*[A-Za-z])(?=.*\d)(?=.*[~!@#\$%\^&\*\(\)\-=_\+])[A-Za-z\d~!@#\$%\^&\*\(\)\-=_\+]{6,}$/);
                    return patten.test(text);
                },
            }
        });
})
    (jQuery);