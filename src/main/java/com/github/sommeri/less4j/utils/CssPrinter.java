package com.github.sommeri.less4j.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.NotACssException;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.AnonymousExpression;
import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.BinaryExpressionOperator;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.CharsetDeclaration;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.CssClass;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.Document;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.EmbeddedScript;
import com.github.sommeri.less4j.core.ast.EmptyExpression;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FaultyNode;
import com.github.sommeri.less4j.core.ast.FixedMediaExpression;
import com.github.sommeri.less4j.core.ast.FontFace;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.IdSelector;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.InlineContent;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.InterpolatedMediaExpression;
import com.github.sommeri.less4j.core.ast.Keyframes;
import com.github.sommeri.less4j.core.ast.KeyframesName;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.MediaExpression;
import com.github.sommeri.less4j.core.ast.MediaExpressionFeature;
import com.github.sommeri.less4j.core.ast.MediaQuery;
import com.github.sommeri.less4j.core.ast.Medium;
import com.github.sommeri.less4j.core.ast.MediumModifier;
import com.github.sommeri.less4j.core.ast.MediumModifier.Modifier;
import com.github.sommeri.less4j.core.ast.MediumType;
import com.github.sommeri.less4j.core.ast.Name;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.Nth;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.Page;
import com.github.sommeri.less4j.core.ast.PageMarginBox;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.PseudoElement;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorAttribute;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorOperator;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.ast.Supports;
import com.github.sommeri.less4j.core.ast.SupportsCondition;
import com.github.sommeri.less4j.core.ast.SupportsConditionInParentheses;
import com.github.sommeri.less4j.core.ast.SupportsConditionNegation;
import com.github.sommeri.less4j.core.ast.SupportsLogicalCondition;
import com.github.sommeri.less4j.core.ast.SupportsLogicalOperator;
import com.github.sommeri.less4j.core.ast.SupportsQuery;
import com.github.sommeri.less4j.core.ast.SyntaxOnlyElement;
import com.github.sommeri.less4j.core.ast.UnicodeRangeExpression;
import com.github.sommeri.less4j.core.ast.UnknownAtRule;
import com.github.sommeri.less4j.core.ast.Viewport;
import com.github.sommeri.less4j.core.output.ExtendedStringBuilder;
import com.github.sommeri.less4j.core.output.SourceMapBuilder;

//EscapedSelector
public class CssPrinter {

    private static final String ERROR = "!#error#!";

    protected ExtendedStringBuilder cssOnly = new ExtendedStringBuilder();

    protected SourceMapBuilder cssAndSM;

    private LessSource lessSource;

    private LessSource cssDestination;

    private LessCompiler.Configuration options;

    public CssPrinter() {
        super();
    }

    public CssPrinter(LessSource lessSource, LessSource cssDestination, LessCompiler.Configuration options) {
        this.lessSource = lessSource;
        this.cssDestination = cssDestination;
        this.options = options;
        this.cssAndSM = new SourceMapBuilder(cssOnly, cssDestination, getSourceMapConfiguration(options));
    }

    public CssPrinter(CssPrinter configureFromPrinter) {
        this.options = configureFromPrinter.options;
        this.lessSource = configureFromPrinter.lessSource;
        this.cssDestination = configureFromPrinter.cssDestination;
        this.cssOnly = new ExtendedStringBuilder(configureFromPrinter.cssOnly);
        this.cssAndSM = new SourceMapBuilder(cssOnly, cssDestination, getSourceMapConfiguration(options));
    }

    private LessCompiler.SourceMapConfiguration getSourceMapConfiguration(LessCompiler.Configuration options) {
        return options != null ? options.getSourceMapConfiguration() : new LessCompiler.SourceMapConfiguration();
    }

    /**
   * returns whether the output changed as a result of the operation
   * 
   * @param node
   * @return
   */
    public boolean append(ASTCssNode node) {
        // thing
        if (node == null || node.isSilent())
            return false;
        appendComments(node.getOpeningComments(), true);
        boolean result = switchOnType(node);
        appendComments(node.getTrailingComments(), false);
        return result;
    }

    public boolean switchOnType(ASTCssNode node) {
        switch(node.getType()) {
            case RULE_SET:
                // TODOsm: source map
                return appendRuleset((RuleSet) node);
            case CSS_CLASS:
                // TODOsm: source map
                return appendCssClass((CssClass) node);
            case PSEUDO_CLASS:
                // TODOsm: source map
                return appendPseudoClass((PseudoClass) node);
            case PSEUDO_ELEMENT:
                // TODOsm: source map
                return appendPseudoElement((PseudoElement) node);
            case NTH:
                // TODOsm: source map
                return appendNth((Nth) node);
            case SELECTOR:
                // TODOsm: source map
                return appendSelector((Selector) node);
            case SIMPLE_SELECTOR:
                // TODOsm: source map
                return appendSimpleSelector((SimpleSelector) node);
            case SELECTOR_OPERATOR:
                // TODOsm: source map
                return appendSelectorOperator((SelectorOperator) node);
            case SELECTOR_COMBINATOR:
                // TODOsm: source map
                return appendSelectorCombinator((SelectorCombinator) node);
            case SELECTOR_ATTRIBUTE:
                // TODOsm: source map
                return appendSelectorAttribute((SelectorAttribute) node);
            case ID_SELECTOR:
                return appendIdSelector((IdSelector) node);
            case CHARSET_DECLARATION:
                return appendCharsetDeclaration((CharsetDeclaration) node);
            case FONT_FACE:
                // TODOsm: source map
                return appendFontFace((FontFace) node);
            case NAMED_EXPRESSION:
                // TODOsm: source map
                return appendNamedExpression((NamedExpression) node);
            case BINARY_EXPRESSION:
                // TODOsm: source map
                return appendComposedExpression((BinaryExpression) node);
            case BINARY_EXPRESSION_OPERATOR:
                // TODOsm: source map
                return appendBinaryExpressionOperator((BinaryExpressionOperator) node);
            case LIST_EXPRESSION:
                // TODOsm: source map
                return appendListExpression((ListExpression) node);
            case LIST_EXPRESSION_OPERATOR:
                // TODOsm: source map
                return appendListExpressionOperator((ListExpressionOperator) node);
            case STRING_EXPRESSION:
                // TODOsm: source map
                return appendCssString((CssString) node);
            case EMPTY_EXPRESSION:
                // TODOsm: source map
                return appendEmptyExpression((EmptyExpression) node);
            case NUMBER:
                // TODOsm: source map
                return appendNumberExpression((NumberExpression) node);
            case IDENTIFIER_EXPRESSION:
                // TODOsm: source map
                return appendIdentifierExpression((IdentifierExpression) node);
            case UNICODE_RANGE_EXPRESSION:
                // TODOsm: source map
                return appendUnicodeRangeExpression((UnicodeRangeExpression) node);
            case COLOR_EXPRESSION:
                // TODOsm: source map
                return appendColorExpression((ColorExpression) node);
            case FUNCTION:
                // TODOsm: source map
                return appendFunctionExpression((FunctionExpression) node);
            case DECLARATION:
                // TODOsm: source map
                return appendDeclaration((Declaration) node);
            case MEDIA:
                // TODOsm: source map
                return appendMedia((Media) node);
            case MEDIA_QUERY:
                // TODOsm: source map
                return appendMediaQuery((MediaQuery) node);
            case MEDIUM:
                // TODOsm: source map
                return appendMedium((Medium) node);
            case MEDIUM_MODIFIER:
                // TODOsm: source map
                return appendMediumModifier((MediumModifier) node);
            case MEDIUM_TYPE:
                // TODOsm: source map
                return appendMediumType((MediumType) node);
            case FIXED_MEDIA_EXPRESSION:
                // TODOsm: source map
                return appendMediaExpression((FixedMediaExpression) node);
            case INTERPOLATED_MEDIA_EXPRESSION:
                // TODOsm: source map
                return appendInterpolatedMediaExpression((InterpolatedMediaExpression) node);
            case MEDIUM_EX_FEATURE:
                // TODOsm: source map
                return appendMediaExpressionFeature((MediaExpressionFeature) node);
            case STYLE_SHEET:
                // TODOsm: source map
                return appendStyleSheet((StyleSheet) node);
            case FAULTY_EXPRESSION:
                // TODOsm: source map
                return appendFaultyExpression((FaultyExpression) node);
            case FAULTY_NODE:
                // TODOsm: source map
                return appendFaultyNode((FaultyNode) node);
            case ESCAPED_VALUE:
                // TODOsm: source map
                return appendEscapedValue((EscapedValue) node);
            case EMBEDDED_SCRIPT:
                // TODOsm: source map
                return appendEmbeddedScript((EmbeddedScript) node);
            case KEYFRAMES:
                // TODOsm: source map
                return appendKeyframes((Keyframes) node);
            case KEYFRAMES_NAME:
                // TODOsm: source map
                return appendKeyframesName((KeyframesName) node);
            case UNKNOWN_AT_RULE:
                // TODOsm: source map
                return appendUnknownAtRule((UnknownAtRule) node);
            case DOCUMENT:
                // TODOsm: source map
                return appendDocument((Document) node);
            case VIEWPORT:
                // TODOsm: source map
                return appendViewport((Viewport) node);
            case GENERAL_BODY:
                // TODOsm: source map
                return appendBodyOptimizeDuplicates((GeneralBody) node);
            case PAGE:
                // TODOsm: source map
                return appendPage((Page) node);
            case PAGE_MARGIN_BOX:
                // TODOsm: source map
                return appendPageMarginBox((PageMarginBox) node);
            case NAME:
                // TODOsm: source map
                return appendName((Name) node);
            case IMPORT:
                // TODOsm: source map
                return appendImport((Import) node);
            case ANONYMOUS:
                // TODOsm: source map
                return appendAnonymous((AnonymousExpression) node);
            case SYNTAX_ONLY_ELEMENT:
                // TODOsm: source map
                return appendSyntaxOnlyElement((SyntaxOnlyElement) node);
            case SUPPORTS:
                // TODOsm: source map
                return appendSupports((Supports) node);
            case SUPPORTS_QUERY:
                // TODOsm: source map
                return appendSupportsQuery((SupportsQuery) node);
            case SUPPORTS_CONDITION_NEGATION:
                // TODOsm: source map
                return appendSupportsConditionNegation((SupportsConditionNegation) node);
            case SUPPORTS_CONDITION_PARENTHESES:
                // TODOsm: source map
                return appendSupportsConditionParentheses((SupportsConditionInParentheses) node);
            case SUPPORTS_CONDITION_LOGICAL:
                // TODOsm: source map
                return appendSupportsConditionLogical((SupportsLogicalCondition) node);
            case SUPPORTS_LOGICAL_OPERATOR:
                // TODOsm: source map
                return appendSupportsLogicalOperator((SupportsLogicalOperator) node);
            case INLINE_CONTENT:
                // TODOsm: source map
                return appendInlineContent((InlineContent) node);
            case ESCAPED_SELECTOR:
            case PARENTHESES_EXPRESSION:
            case SIGNED_EXPRESSION:
            case VARIABLE:
            case DETACHED_RULESET:
            case DETACHED_RULESET_REFERENCE:
            case INDIRECT_VARIABLE:
            case VARIABLE_DECLARATION:
                throw new NotACssException(node);
            default:
                throw new IllegalStateException("Unknown: " + node.getType() + " " + node.getSourceLine() + ":" + node.getSourceColumn());
        }
    }

    public boolean appendCommaSeparated(List<? extends ASTCssNode> values) {
        boolean result = false;
        Iterator<? extends ASTCssNode> names = values.iterator();
        if (names.hasNext()) {
            result |= append(names.next());
        }
        while (names.hasNext()) {
            cssOnly.append(",").ensureSeparator();
            append(names.next());
            result = true;
        }
        return result;
    }

    //TODO: what about source maps?
    private boolean appendInlineContent(InlineContent node) {
        cssOnly.appendAsIs(node.getValue());
        return false;
    }

    private boolean appendSyntaxOnlyElement(SyntaxOnlyElement node) {
        cssOnly.append(node.getSymbol());
        return true;
    }

    private boolean appendAnonymous(AnonymousExpression node) {
        cssOnly.append(node.getValue());
        return true;
    }

    private boolean appendImport(Import node) {
        cssOnly.append("@import").ensureSeparator();
        append(node.getUrlExpression());
        appendCommaSeparated(node.getMediums());
        cssOnly.append(";");
        return true;
    }

    private boolean appendName(Name node) {
        cssOnly.append(node.getName());
        return true;
    }

    private boolean appendPage(Page node) {
        cssOnly.append("@page").ensureSeparator();
        if (node.hasName()) {
            append(node.getName());
            if (!node.hasDockedPseudopage())
                cssOnly.ensureSeparator();
        }
        if (node.hasPseudopage()) {
            append(node.getPseudopage());
            cssOnly.ensureSeparator();
        }
        appendBodySortDeclarations(node.getBody());
        return true;
    }

    private boolean appendPageMarginBox(PageMarginBox node) {
        append(node.getName());
        appendBodySortDeclarations(node.getBody());
        return true;
    }

    private boolean appendKeyframesName(KeyframesName node) {
        append(node.getName());
        return true;
    }

    private boolean appendKeyframes(Keyframes node) {
        cssOnly.append(node.getDialect()).ensureSeparator();
        appendCommaSeparated(node.getNames());
        append(node.getBody());
        return true;
    }

    private boolean appendUnknownAtRule(UnknownAtRule node) {
        cssOnly.append(node.getName()).ensureSeparator();
        appendCommaSeparated(node.getNames());
        append(node.getBody());
        append(node.getSemicolon());
        return true;
    }

    private boolean appendSupports(Supports node) {
        cssOnly.append(node.getDialect()).ensureSeparator();
        append(node.getCondition());
        cssOnly.ensureSeparator();
        append(node.getBody());
        return true;
    }

    private boolean appendSupportsConditionNegation(SupportsConditionNegation node) {
        append(node.getNegation());
        cssOnly.ensureSeparator();
        append(node.getCondition());
        return true;
    }

    private boolean appendSupportsConditionParentheses(SupportsConditionInParentheses node) {
        append(node.getOpeningParentheses());
        append(node.getCondition());
        append(node.getClosingParentheses());
        return true;
    }

    private boolean appendSupportsConditionLogical(SupportsLogicalCondition node) {
        Iterator<SupportsLogicalOperator> operators = node.getLogicalOperators().iterator();
        Iterator<SupportsCondition> conditions = node.getConditions().iterator();
        append(conditions.next());
        while (operators.hasNext()) {
            cssOnly.ensureSeparator();
            append(operators.next());
            cssOnly.ensureSeparator();
            append(conditions.next());
        }
        return true;
    }

    private boolean appendSupportsLogicalOperator(SupportsLogicalOperator node) {
        if (node.getOperator() == null) {
            cssOnly.append(ERROR);
        }
        cssOnly.append(node.getOperator().getSymbol());
        return true;
    }

    private boolean appendSupportsQuery(SupportsQuery node) {
        append(node.getOpeningParentheses());
        append(node.getDeclaration());
        append(node.getClosingParentheses());
        return true;
    }

    private boolean appendDocument(Document node) {
        cssOnly.append(node.getDialect()).ensureSeparator();
        appendCommaSeparated(node.getUrlMatchFunctions());
        append(node.getBody());
        return true;
    }

    private boolean appendViewport(Viewport node) {
        cssOnly.append(node.getDialect());
        append(node.getBody());
        return true;
    }

    private boolean appendFaultyNode(FaultyNode node) {
        cssOnly.append(ERROR);
        return true;
    }

    private boolean appendFaultyExpression(FaultyExpression node) {
        cssOnly.append(ERROR);
        return true;
    }

    private boolean appendNth(Nth node) {
        switch(node.getForm()) {
            case EVEN:
                cssOnly.append("even");
                return true;
            case ODD:
                cssOnly.append("odd");
                return true;
            case STANDARD:
                if (node.getRepeater() != null)
                    append(node.getRepeater());
                if (node.getMod() != null)
                    append(node.getMod());
        }
        return true;
    }

    protected void appendComments(List<Comment> comments, boolean ensureSeparator) {
        if (comments == null || comments.isEmpty())
            return;
        cssOnly.ensureSeparator();
        for (Comment comment : comments) {
            cssOnly.append(comment.getComment());
            if (comment.hasNewLine())
                cssOnly.newLine();
        }
        if (ensureSeparator)
            cssOnly.ensureSeparator();
    }

    public boolean appendFontFace(FontFace node) {
        cssOnly.append("@font-face").ensureSeparator();
        append(node.getBody());
        return true;
    }

    public boolean appendCharsetDeclaration(CharsetDeclaration node) {
        cssAndSM.append("@charset", node.getUnderlyingStructure()).ensureSeparator();
        append(node.getCharset());
        cssOnly.append(";");
        return true;
    }

    public boolean appendIdSelector(IdSelector node) {
        cssAndSM.append(node.getFullName(), node.getUnderlyingStructure());
        return true;
    }

    public boolean appendSelectorAttribute(SelectorAttribute node) {
        cssOnly.append("[");
        cssOnly.append(node.getName());
        append(node.getOperator());
        append(node.getValue());
        cssOnly.append("]");
        return true;
    }

    private boolean appendSelectorOperator(SelectorOperator operator) {
        SelectorOperator.Operator realOperator = operator.getOperator();
        switch(realOperator) {
            case NONE:
                break;
            default:
                cssOnly.append(realOperator.getSymbol());
        }
        return true;
    }

    public boolean appendPseudoClass(PseudoClass node) {
        cssOnly.append(":");
        cssOnly.append(node.getName());
        if (node.hasParameters()) {
            cssOnly.append("(");
            append(node.getParameter());
            cssOnly.append(")");
        }
        return true;
    }

    public boolean appendPseudoElement(PseudoElement node) {
        cssOnly.append(":");
        if (!node.isLevel12Form())
            cssOnly.append(":");
        cssOnly.append(node.getName());
        return true;
    }

    public boolean appendStyleSheet(StyleSheet styleSheet) {
        appendComments(styleSheet.getOrphanComments(), false);
        appendAllChilds(styleSheet);
        return true;
    }

    public boolean appendRuleset(RuleSet ruleSet) {
        if (ruleSet.hasEmptyBody())
            return false;
        appendSelectors(ruleSet.getSelectors());
        append(ruleSet.getBody());
        return true;
    }

    private boolean appendBodyOptimizeDuplicates(Body body) {
        if (body.isEmpty())
            return false;
        cssOnly.ensureSeparator();
        append(body.getOpeningCurlyBrace());
        cssOnly.ensureNewLine().increaseIndentationLevel();
        Iterable<CssPrinter> declarationsBuilders = collectUniqueBodyMembersStrings(body);
        for (CssPrinter miniBuilder : declarationsBuilders) {
            append(miniBuilder);
        }
        appendComments(body.getOrphanComments(), false);
        cssOnly.decreaseIndentationLevel();
        append(body.getClosingCurlyBrace());
        return true;
    }

    private void append(CssPrinter miniBuilder) {
        cssAndSM.append(miniBuilder.cssAndSM);
    }

    private Iterable<CssPrinter> collectUniqueBodyMembersStrings(Body body) {
        // the same declaration must be printed only once
        LastOfKindSet<String, CssPrinter> declarationsStrings = new LastOfKindSet<String, CssPrinter>();
        for (ASTCssNode declaration : body.getMembers()) {
            CssPrinter miniPrinter = new CssPrinter(this);
            miniPrinter.append(declaration);
            miniPrinter.cssOnly.ensureNewLine();
            declarationsStrings.add(miniPrinter.toCss(), miniPrinter);
        }
        return declarationsStrings;
    }

    public boolean appendDeclaration(Declaration declaration) {
        cssOnly.appendIgnoreNull(declaration.getNameAsString());
        cssOnly.append(":").ensureSeparator();
        if (declaration.getExpression() != null)
            append(declaration.getExpression());
        if (declaration.isImportant())
            cssOnly.ensureSeparator().append("!important");
        if (shouldHaveSemicolon(declaration))
            cssOnly.appendIgnoreNull(";");
        return true;
    }

    private boolean shouldHaveSemicolon(Declaration declaration) {
        if (null == declaration.getParent() || declaration.getParent().getType() != ASTCssNodeType.SUPPORTS_QUERY)
            return true;
        return false;
    }

    private boolean appendMedia(Media node) {
        cssOnly.append("@media");
        appendCommaSeparated(node.getMediums());
        appendBodySortDeclarations(node.getBody());
        return true;
    }

    private void appendBodySortDeclarations(Body node) {
        // this is sort of hack, bypass the usual append method
        appendComments(node.getOpeningComments(), true);
        cssOnly.ensureSeparator();
        append(node.getOpeningCurlyBrace());
        cssOnly.ensureNewLine().increaseIndentationLevel();
        Iterator<ASTCssNode> declarations = node.getDeclarations().iterator();
        List<ASTCssNode> notDeclarations = node.getNotDeclarations();
        while (declarations.hasNext()) {
            ASTCssNode declaration = declarations.next();
            append(declaration);
            if (declarations.hasNext() || notDeclarations.isEmpty())
                cssOnly.ensureNewLine();
        }
        for (ASTCssNode body : notDeclarations) {
            boolean changedAnything = append(body);
            if (changedAnything)
                cssOnly.ensureNewLine();
        }
        appendComments(node.getOrphanComments(), false);
        cssOnly.decreaseIndentationLevel();
        append(node.getClosingCurlyBrace());
        // this is sort of hack, bypass the usual append method
        appendComments(node.getTrailingComments(), false);
    }

    public boolean appendMediaQuery(MediaQuery mediaQuery) {
        cssOnly.ensureSeparator();
        append(mediaQuery.getMedium());
        boolean needSeparator = (mediaQuery.getMedium() != null);
        for (MediaExpression mediaExpression : mediaQuery.getExpressions()) {
            if (needSeparator) {
                cssOnly.ensureSeparator().append("and");
            }
            append(mediaExpression);
            needSeparator = true;
        }
        return true;
    }

    public boolean appendMedium(Medium medium) {
        append(medium.getModifier());
        append(medium.getMediumType());
        return true;
    }

    public boolean appendMediumModifier(MediumModifier modifier) {
        Modifier kind = modifier.getModifier();
        switch(kind) {
            case ONLY:
                cssOnly.ensureSeparator().append("only");
                break;
            case NOT:
                cssOnly.ensureSeparator().append("not");
                break;
            case NONE:
                break;
            default:
                throw new IllegalStateException("Unknown modifier type.");
        }
        return true;
    }

    public boolean appendMediumType(MediumType medium) {
        cssOnly.ensureSeparator().append(medium.getName());
        return true;
    }

    public boolean appendMediaExpression(FixedMediaExpression expression) {
        cssOnly.ensureSeparator().append("(");
        append(expression.getFeature());
        if (expression.getExpression() != null) {
            cssOnly.append(":").ensureSeparator();
            append(expression.getExpression());
        }
        cssOnly.append(")");
        return true;
    }

    private boolean appendInterpolatedMediaExpression(InterpolatedMediaExpression expression) {
        cssOnly.ensureSeparator();
        return append(expression.getExpression());
    }

    public boolean appendMediaExpressionFeature(MediaExpressionFeature feature) {
        cssOnly.append(feature.getFeature());
        return true;
    }

    public boolean appendNamedExpression(NamedExpression expression) {
        cssOnly.append(expression.getName());
        cssOnly.append("=");
        append(expression.getExpression());
        return true;
    }

    public boolean appendComposedExpression(BinaryExpression expression) {
        append(expression.getLeft());
        append(expression.getOperator());
        append(expression.getRight());
        return true;
    }

    public boolean appendListExpression(ListExpression expression) {
        Iterator<Expression> iterator = expression.getExpressions().iterator();
        if (!iterator.hasNext())
            return false;
        append(iterator.next());
        while (iterator.hasNext()) {
            append(expression.getOperator());
            append(iterator.next());
        }
        return true;
    }

    public boolean appendExpressionOperator(ListExpressionOperator operator) {
        ListExpressionOperator.Operator realOperator = operator.getOperator();
        switch(realOperator) {
            case COMMA:
                cssOnly.append(realOperator.getSymbol()).ensureSeparator();
                break;
            case EMPTY_OPERATOR:
                cssOnly.ensureSeparator();
                break;
            default:
                cssOnly.append(realOperator.getSymbol());
        }
        return true;
    }

    public boolean appendBinaryExpressionOperator(BinaryExpressionOperator operator) {
        BinaryExpressionOperator.Operator realOperator = operator.getOperator();
        switch(realOperator) {
            // back to it later
            case MINUS:
                cssOnly.ensureSeparator().append("-");
                break;
            default:
                cssOnly.append(realOperator.getSymbol());
        }
        return true;
    }

    public boolean appendListExpressionOperator(ListExpressionOperator operator) {
        ListExpressionOperator.Operator realOperator = operator.getOperator();
        switch(realOperator) {
            case COMMA:
                cssOnly.append(realOperator.getSymbol()).ensureSeparator();
                break;
            case EMPTY_OPERATOR:
                cssOnly.ensureSeparator();
                break;
            default:
                cssOnly.append(realOperator.getSymbol());
        }
        return true;
    }

    public boolean appendCssString(CssString expression) {
        String quoteType = expression.getQuoteType();
        cssOnly.append(quoteType).append(expression.getValue()).append(quoteType);
        return true;
    }

    public boolean appendEmptyExpression(EmptyExpression node) {
        return true;
    }

    public boolean appendEscapedValue(EscapedValue escaped) {
        cssOnly.append(escaped.getValue());
        return true;
    }

    public boolean appendEmbeddedScript(EmbeddedScript escaped) {
        cssOnly.append(escaped.getValue());
        return true;
    }

    public boolean appendIdentifierExpression(IdentifierExpression expression) {
        cssOnly.append(expression.getValue());
        return true;
    }

    public boolean appendUnicodeRangeExpression(UnicodeRangeExpression expression) {
        cssOnly.append(expression.getValue());
        return true;
    }

    protected boolean appendColorExpression(ColorExpression expression) {
        // if it is named color expression, write out the name
        if (expression instanceof NamedColorExpression) {
            NamedColorExpression named = (NamedColorExpression) expression;
            cssOnly.append(named.getColorName());
        } else {
            cssOnly.append(expression.getValue());
        }
        return true;
    }

    private boolean appendFunctionExpression(FunctionExpression node) {
        cssOnly.append(node.getName());
        cssOnly.append("(");
        append(node.getParameter());
        cssOnly.append(")");
        return true;
    }

    private boolean appendNumberExpression(NumberExpression node) {
        if (node.hasOriginalString()) {
            cssOnly.append(node.getOriginalString());
        } else {
            if (node.hasExpliciteSign()) {
                if (0 < node.getValueAsDouble())
                    cssOnly.append("+");
                else
                    cssOnly.append("-");
            }
            cssOnly.append(PrintUtils.formatNumber(node.getValueAsDouble()) + node.getSuffix());
        }
        return true;
    }

    public void appendSelectors(List<Selector> selectors) {
        selectors = filterSilent(selectors);
        // follow less.js formatting in special case - only one empty selector
        if (selectors.size() == 1 && isEmptySelector(selectors.get(0))) {
            cssOnly.append(" ");
        }
        Iterator<Selector> iterator = selectors.iterator();
        while (iterator.hasNext()) {
            Selector selector = iterator.next();
            append(selector);
            if (iterator.hasNext())
                cssOnly.append(",").newLine();
        }
    }

    private <T extends ASTCssNode> List<T> filterSilent(List<T> nodes) {
        List<T> result = new ArrayList<T>();
        for (T t : nodes) {
            if (!t.isSilent())
                result.add(t);
        }
        return result;
    }

    private boolean isEmptySelector(Selector selector) {
        if (selector.isCombined())
            return false;
        SelectorPart head = selector.getHead();
        if (head.getType() != ASTCssNodeType.SIMPLE_SELECTOR)
            return false;
        SimpleSelector simpleHead = (SimpleSelector) head;
        if (!simpleHead.isEmptyForm() || !simpleHead.isStar()) {
            return false;
        }
        if (simpleHead.hasSubsequent())
            return false;
        return true;
    }

    public boolean appendSelector(Selector selector) {
        for (SelectorPart part : selector.getParts()) {
            append(part);
        }
        return true;
    }

    private boolean appendSimpleSelector(SimpleSelector selector) {
        if (selector.hasLeadingCombinator())
            append(selector.getLeadingCombinator());
        appendSimpleSelectorHead(selector);
        appendSimpleSelectorTail(selector);
        return true;
    }

    private void appendSimpleSelectorTail(SimpleSelector selector) {
        List<ElementSubsequent> allChilds = selector.getSubsequent();
        for (ElementSubsequent astCssNode : allChilds) {
            append(astCssNode);
        }
    }

    private boolean appendCssClass(CssClass cssClass) {
        //cssOnly.append(cssClass.getFullName());
        cssAndSM.append(cssClass.getFullName(), cssClass.getUnderlyingStructure());
        return true;
    }

    private void appendSimpleSelectorHead(SimpleSelector selector) {
        cssOnly.ensureSeparator();
        if (!selector.isStar() || !selector.isEmptyForm()) {
            InterpolableName elementName = selector.getElementName();
            cssAndSM.appendIgnoreNull(elementName.getName(), elementName.getUnderlyingStructure());
        }
    }

    public boolean appendSelectorCombinator(SelectorCombinator combinator) {
        SelectorCombinator.Combinator realCombinator = combinator.getCombinator();
        switch(realCombinator) {
            case DESCENDANT:
                cssOnly.ensureSeparator();
                break;
            default:
                cssOnly.ensureSeparator().append(realCombinator.getSymbol());
        }
        return true;
    }

    private void appendAllChilds(ASTCssNode node) {
        List<? extends ASTCssNode> allChilds = node.getChilds();
        appendAll(allChilds);
    }

    private void appendAll(List<? extends ASTCssNode> all) {
        for (ASTCssNode kid : all) {
            if (append(kid))
                cssOnly.ensureNewLine();
        }
    }

    public String toString() {
        return cssOnly.toString();
    }

    public String toCss() {
        return cssOnly.toString();
    }

    public String toSourceMap() {
        return cssAndSM.toSourceMap();
    }
}
