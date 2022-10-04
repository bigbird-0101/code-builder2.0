package com.fpp.code.core.template.languagenode;

/**
 * 表示整个模板的后缀节点 用于可控制方法的模板 作为后缀
 * 类似
 * <suffix>
 *    abc
 * </suffix>
 *   .
 *   .
 *   .
 * @author bigbird-0101
 * @version 1.0.0
 * @since 2022-10-04 10:58:33
 */
public class SuffixCodeNode extends WrapperCodeNode {
    public SuffixCodeNode(CodeNode codeNode) {
        super(codeNode);
    }
}