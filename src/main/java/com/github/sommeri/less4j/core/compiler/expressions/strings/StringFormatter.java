package com.github.sommeri.less4j.core.compiler.expressions.strings;

import java.util.Iterator;
import java.util.regex.Pattern;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.InStringCssPrinter;
import com.github.sommeri.less4j.utils.PrintUtils;
import com.github.sommeri.less4j.utils.QuotesKeepingInStringCssPrinter;

public class StringFormatter extends AbstractStringReplacer<Iterator<Expression>> {

  private static final Pattern PLACEHOLDER = Pattern.compile("%.");

  @Override
  protected Pattern getPattern() {
    return PLACEHOLDER;
  }

  @Override
  protected String extractMatchName(String group) {
    return group.substring(1);
  }

  @Override
  protected String replacementValue(Iterator<Expression> replacements, HiddenTokenAwareTree technicalUnderlying, MatchRange matchRange) {
    String value = getValue(matchRange.getName(), replacements);
    if (value==null)
      return matchRange.getFullMatch();
    
    if (shouldEscape(matchRange)) {
      return PrintUtils.toUtf8ExceptGrrr(value);
    }
    return value;
  }

  private boolean shouldEscape(MatchRange matchRange) {
    return Character.isUpperCase(matchRange.getName().charAt(0));
  }

  private String getValue(String name, Iterator<Expression> replacements) {
    if (!replacements.hasNext())
      return null;
    
    if ("%".equalsIgnoreCase(name)) {
      return "%";
    }

    Expression replacement = replacements.next();
    // special case: imitating less.js behavior on incorrect color input
    if ("S".equals(name) && replacement.getType()==ASTCssNodeType.COLOR_EXPRESSION) {
      return "undefined";
    }

    if ("s".equalsIgnoreCase(name)) {
      InStringCssPrinter printer = new InStringCssPrinter();
      printer.append(validate(replacement));
      return printer.toString();
    } else if ("d".equalsIgnoreCase(name) || "a".equalsIgnoreCase(name)) {
      QuotesKeepingInStringCssPrinter printer = new QuotesKeepingInStringCssPrinter();
      printer.append(replacement);
      return printer.toString();
    } 

    return null;
  }

  private Expression validate(Expression replacement) {
    if (replacement.getType() != ASTCssNodeType.COLOR_EXPRESSION)
      return replacement;

    if (replacement instanceof NamedColorExpression)
      return new IdentifierExpression(replacement.getUnderlyingStructure(), "");

    return new IdentifierExpression(replacement.getUnderlyingStructure(), "");
  }
}
