// === LICENSE_START ===
// # LICENSE
// 
// This software is licensed only under the T. Hino Commercial License
// (THCL) v1.0. Use, copying, modification, distribution, academic use,
// commercial use, and use by corporations or legal entities require
// compliance with the terms below.
// 
// ---
// 
// ## T. Hino Commercial License (THCL) v1.0
// 
// Copyright (c) 2026 T. Hino. All rights reserved.
// 
// This license governs the use of ProgramLanguageLogicConvertor
// (hereinafter "the Software"), developed by T. Hino (hereinafter "the Author").
// 
// 1. Grant of License
//    Any person or entity wishing to use, copy, modify, distribute, or
//    otherwise handle the Software must submit a usage application to the
//    Author and obtain written or electronic approval before a license is
//    granted.
//    Any use without such approval shall be deemed copyright infringement.
// 
//    Electronic records include:
//    - Email
//    - Comments made by the Author on the Software's repository
// 
// 2. License Term
//    The license is valid for one (1) year from the date of grant.
//    To continue use, a renewal application must be submitted to the Author
//    no later than thirty (30) days before expiration, and re-approval must
//    be obtained.
// 
// 3. License Fee
//    The license fee shall be determined separately by mutual agreement
//    between the Author and the licensee.
//    If the license is granted free of charge, such agreement shall be
//    explicitly stated in writing or electronic record.
//    The Author reserves the right to set a new license fee upon each renewal.
// 
// 4. Effect of License Expiration
//    If renewal is not approved, the license to use the Software itself
//    shall expire at the end of the license term.
//    However, any output or deliverables (e.g., converted source code)
//    generated using the Software during the valid license period may
//    continue to be used after license expiration.
// 
// 5. Restriction on Modification and Redistribution
//    Any modification or redistribution of the Software requires separate
//    written or electronic approval from the Author.
//    Use, distribution, or publication of modified versions without such
//    approval shall constitute a violation of this license.
// 
// 6. Retention of Copyright Notice
//    The following copyright notice must be retained in all copies and
//    derivative works of the Software:
// 
//    "Copyright (c) 2026 T. Hino. Licensed under THCL."
// 
//    The method of retention shall be as follows depending on usage:
// 
//    (a) When copying or modifying source code:
//        The above notice must be included in a comment at the top of
//        each source file.
// 
//    (b) When distributing in binary or executable form:
//        At least one of the following must be satisfied:
//        - Include the above notice in the application's About dialog
//        - Include the above notice in documentation (e.g., README)
//          bundled with the distribution
// 
//    (c) When used as an internal tool or system:
//        The above notice must be included in the help screen or
//        version information screen of the system.
// 
//    Modification or deletion of the above notice shall constitute
//    a violation of this license.
// 
// 7. Disclaimer
//    The Software is provided "as is" without warranty of any kind.
//    The Author shall not be liable for any damages arising from the
//    use of the Software.
// 
// 8. Citation Requirement for Academic Use
//    When the logic, algorithms, or design concepts of the Software are
//    used or referenced in papers, technical documents, academic presentations,
//    or similar works, the Author and the Software must be explicitly cited
//    in the following format:
// 
//    [Citation Format]
//    T. Hino, "ProgramLanguageLogicConvertor", GitHub,
//    https://github.com/takahino/ProgramLanguageLogicConvertor, [Date Accessed]
// 
//    Academic use without proper citation shall constitute a violation
//    of this license.
//    If a citation is made, it is recommended that the Author be notified
//    via email or a repository Issue.
// 
// ---
// 
// Contact  : takahino@ymail.ne.jp
// Inquiries: https://github.com/takahino/ProgramLanguageLogicConvertor/issues
// Repository: https://github.com/takahino/ProgramLanguageLogicConvertor
// 
// ---
// 
// ## Applicable License
// 
// All use cases are governed by THCL v1.0. A usage application and approval
// from the Author are required before use unless the Author has separately
// granted permission in writing or electronic record.
// === LICENSE_END ===

package io.github.takahino.cpp2csharp.matcher;

import io.github.takahino.cpp2csharp.grammar.CPP14Parser;
import io.github.takahino.cpp2csharp.grammar.CPP14ParserBaseVisitor;
import io.github.takahino.cpp2csharp.matcher.ReceiverValidator;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * RECEIVER00 のキャプチャ妥当性を ANTLR 再パースで検証するクラス。
 *
 * <p>
 * captured を C++ 式として再パースし、postfix chain（識別子・メンバアクセス・
 * 関数呼び出し・添字アクセスの連鎖）のみを許可する。二項演算・三項演算・代入・ cast・prefix unary は拒否する。
 * </p>
 *
 * <p>
 * 本クラスはプリフィルタ（{@link ReceiverCapturePolicy}）通過後の本判定として使用する。
 * </p>
 *
 * <p>
 * 関連仕様: {@code docs/receiver_validation_spec.md}
 * </p>
 */
public final class ReceiverAstValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverAstValidator.class);

	/**
	 * isValid() 結果のメモ化キャッシュ。 キー: CPP14Lexer が出力したトークン型番号の列（例: "99,46,99"）。
	 * 具体的な識別子名には依存しないため、"this . m_str" と "this . m_str2" は
	 * どちらも同じキーにマップされ、ヒット率が大幅に向上する。 ReceiverShapeVisitor は文法構造のみを検査し識別子の値を参照しないため正確。
	 * 上限 500 エントリ（LRU 方式）で十分。構造パターンの種類は文字列種類より遥かに少ない。
	 */
	private static final Cache<String, Boolean> VALIDITY_CACHE = CacheBuilder.newBuilder().maximumSize(500).build();

	/**
	 * テキストレベルのプレキャッシュ。キー: 式文字列（例: "this . m_listCtrl"）。
	 *
	 * <p>
	 * 型キャッシュ（{@link #VALIDITY_CACHE}）がヒットしても Lexer 自体のコストが残る問題を解決する。
	 * 同一式文字列に対するヒットでは lex を完全にスキップできる。 MFC コードでは {@code this -> m_listCtrl}
	 * のような受信者が数百回繰り返されるためヒット率が高い。 結果は決定的なため {@link ConcurrentHashMap} で安全に共有できる。
	 * </p>
	 */
	private static final ConcurrentHashMap<String, Boolean> TEXT_CACHE = new ConcurrentHashMap<>();

	private ReceiverAstValidator() {
		throw new AssertionError("utility class");
	}

	/**
	 * ReceiverValidator インターフェースの実装として、AST 妥当性検証を提供するインスタンスを返す。
	 *
	 * @return {@link ReceiverValidator} 実装
	 */
	public static ReceiverValidator asValidator() {
		return captured -> isValid(captured);
	}

	/**
	 * プリフィルタ + AST 判定を組み合わせた RECEIVER フィルタを返す。
	 *
	 * @return 候補が有効な receiver なら true
	 */
	public static Predicate<List<String>> createFilter() {
		return captured -> {
			if (captured.size() == 1)
				return true;
			if (!ReceiverCapturePolicy.passesPrefilter(captured))
				return false;
			return isValid(captured);
		};
	}

	/**
	 * 診断モード用の RECEIVER フィルタを返す。
	 *
	 * @return 候補が診断用に有効なら true
	 */
	public static Predicate<List<String>> createFilterForDiagnostic() {
		// 診断モードは「まだ rule に掛かりそうな候補」を広めに拾いたいので、
		// 厳密な AST 判定は掛けず、明白な不正だけをプリフィルタで落とす。
		return ReceiverCapturePolicy::passesPrefilterForDiagnostic;
	}

	/**
	 * キャプチャされたトークン列が有効な receiver 式（postfix chain）かどうかを ANTLR で再パースして判定する。
	 *
	 * @param captured
	 *            RECEIVER にキャプチャされたトークン列
	 * @return postfix chain として有効であれば true
	 */
	public static boolean isValid(List<String> captured) {
		if (captured.isEmpty())
			return false;

		String expr = String.join(" ", captured);

		// テキストプレキャッシュ: 同一式文字列なら Lexer 実行をスキップする。
		// MFC コードでは同じレシーバー式が多数回繰り返されるためヒット率が高い。
		Boolean textCached = TEXT_CACHE.get(expr);
		if (textCached != null)
			return textCached;

		// Lexer を一度だけ実行してトークン型列を取得する。
		// キャッシュキーをトークン型列にすることで、"this . m_str" と "this . m_str2" など
		// 識別子名だけが異なる構造的に等価なパターンが同一エントリにマップされ、
		// ヒット率が大幅に向上する。
		CommonTokenStream tokenStream = CppParserFactory.lex(expr);
		String typeKey = buildTypeKey(tokenStream);

		try {
			boolean result = VALIDITY_CACHE.get(typeKey, () -> parseAndValidate(tokenStream, expr));
			TEXT_CACHE.putIfAbsent(expr, result);
			return result;
		} catch (ExecutionException e) {
			return false;
		}
	}

	/**
	 * Lexer 出力のトークン型番号列をカンマ区切り文字列で返す。 デフォルトチャンネルのトークンのみ対象とし、EOF は除く。
	 */
	private static String buildTypeKey(CommonTokenStream tokenStream) {
		return tokenStream.getTokens().stream()
				.filter(t -> t.getChannel() == Token.DEFAULT_CHANNEL && t.getType() != Token.EOF)
				.map(t -> String.valueOf(t.getType())).collect(Collectors.joining(","));
	}

	/**
	 * キャッシュ callable として切り出した実パース処理。 Lexer 済みの tokenStream をそのまま Parser
	 * に渡し、二重レキシングを避ける。
	 */
	private static boolean parseAndValidate(CommonTokenStream tokenStream, String exprForLog) {
		try {
			CPP14Parser parser = CppParserFactory.createParser(tokenStream);

			ParseTree tree = parser.expression();
			if (parser.getCurrentToken().getType() != Token.EOF) {
				return false;
			}
			return new ReceiverShapeVisitor().visit(tree);
		} catch (ParseCancellationException e) {
			// 正常系: C++ 式として構文エラー → receiver 不正
			return false;
		} catch (Exception e) {
			LOGGER.warn("RECEIVER00 AST validation failed unexpectedly: {}", exprForLog, e);
			return false;
		}
	}

	/**
	 * 式が postfix chain のみに還元されるかどうかを判定するビジター。 二項演算・三項・代入・cast・prefix unary を検出したら即
	 * false を返す。
	 */
	private static class ReceiverShapeVisitor extends CPP14ParserBaseVisitor<Boolean> {

		/** 除外: カンマ式 {@code a, b} */
		@Override
		public Boolean visitExpression(CPP14Parser.ExpressionContext ctx) {
			if (ctx.assignmentExpression().size() > 1)
				return false;
			return visit(ctx.assignmentExpression(0));
		}

		/** 除外: 代入式 {@code x = y}、throw 式 */
		@Override
		public Boolean visitAssignmentExpression(CPP14Parser.AssignmentExpressionContext ctx) {
			if (ctx.assignmentOperator() != null)
				return false;
			if (ctx.throwExpression() != null)
				return false;
			return visit(ctx.conditionalExpression());
		}

		/** 除外: 三項演算 {@code cond ? x : y} */
		@Override
		public Boolean visitConditionalExpression(CPP14Parser.ConditionalExpressionContext ctx) {
			if (ctx.Question() != null)
				return false;
			return visit(ctx.logicalOrExpression());
		}

		/** 除外: 論理 OR {@code a || b} */
		@Override
		public Boolean visitLogicalOrExpression(CPP14Parser.LogicalOrExpressionContext ctx) {
			if (ctx.logicalAndExpression().size() > 1)
				return false;
			return visit(ctx.logicalAndExpression(0));
		}

		/** 除外: 論理 AND {@code a && b} */
		@Override
		public Boolean visitLogicalAndExpression(CPP14Parser.LogicalAndExpressionContext ctx) {
			if (ctx.inclusiveOrExpression().size() > 1)
				return false;
			return visit(ctx.inclusiveOrExpression(0));
		}

		/** 除外: ビット OR {@code a | b} */
		@Override
		public Boolean visitInclusiveOrExpression(CPP14Parser.InclusiveOrExpressionContext ctx) {
			if (ctx.exclusiveOrExpression().size() > 1)
				return false;
			return visit(ctx.exclusiveOrExpression(0));
		}

		/** 除外: ビット XOR {@code a ^ b} */
		@Override
		public Boolean visitExclusiveOrExpression(CPP14Parser.ExclusiveOrExpressionContext ctx) {
			if (ctx.andExpression().size() > 1)
				return false;
			return visit(ctx.andExpression(0));
		}

		/** 除外: ビット AND {@code a & b} */
		@Override
		public Boolean visitAndExpression(CPP14Parser.AndExpressionContext ctx) {
			if (ctx.equalityExpression().size() > 1)
				return false;
			return visit(ctx.equalityExpression(0));
		}

		/** 除外: 等価 {@code a == b}, {@code a != b} */
		@Override
		public Boolean visitEqualityExpression(CPP14Parser.EqualityExpressionContext ctx) {
			if (ctx.relationalExpression().size() > 1)
				return false;
			return visit(ctx.relationalExpression(0));
		}

		/** 除外: 比較 {@code a < b}, {@code a > b} 等 */
		@Override
		public Boolean visitRelationalExpression(CPP14Parser.RelationalExpressionContext ctx) {
			if (ctx.shiftExpression().size() > 1)
				return false;
			return visit(ctx.shiftExpression(0));
		}

		/** 除外: シフト {@code a << b}, {@code a >> b} */
		@Override
		public Boolean visitShiftExpression(CPP14Parser.ShiftExpressionContext ctx) {
			if (ctx.additiveExpression().size() > 1)
				return false;
			return visit(ctx.additiveExpression(0));
		}

		/** 除外: 加減算 {@code a + b}, {@code a - b} */
		@Override
		public Boolean visitAdditiveExpression(CPP14Parser.AdditiveExpressionContext ctx) {
			if (ctx.multiplicativeExpression().size() > 1)
				return false;
			return visit(ctx.multiplicativeExpression(0));
		}

		/** 除外: 乗除算 {@code a * b}, {@code a / b}, {@code a % b} */
		@Override
		public Boolean visitMultiplicativeExpression(CPP14Parser.MultiplicativeExpressionContext ctx) {
			if (ctx.pointerMemberExpression().size() > 1)
				return false;
			return visit(ctx.pointerMemberExpression(0));
		}

		/** 除外: メンバポインタ {@code a .* b}, {@code a ->* b} */
		@Override
		public Boolean visitPointerMemberExpression(CPP14Parser.PointerMemberExpressionContext ctx) {
			if (ctx.castExpression().size() > 1)
				return false;
			return visit(ctx.castExpression(0));
		}

		/** 除外: C スタイル cast {@code (CString)x} */
		@Override
		public Boolean visitCastExpression(CPP14Parser.CastExpressionContext ctx) {
			if (ctx.theTypeId() != null)
				return false;
			return visit(ctx.unaryExpression());
		}

		/**
		 * 除外: prefix unary {@code *ptr}, {@code &x}, {@code !a}, {@code ++x},
		 * {@code sizeof}, new, delete, noexcept, alignof
		 */
		@Override
		public Boolean visitUnaryExpression(CPP14Parser.UnaryExpressionContext ctx) {
			if (ctx.unaryOperator() != null)
				return false;
			if (ctx.PlusPlus() != null)
				return false;
			if (ctx.MinusMinus() != null)
				return false;
			if (ctx.Sizeof() != null)
				return false;
			if (ctx.noExceptExpression() != null)
				return false;
			if (ctx.newExpression_() != null)
				return false;
			if (ctx.deleteExpression() != null)
				return false;
			if (ctx.Alignof() != null)
				return false;
			return visit(ctx.postfixExpression());
		}

		/** 許可: 括弧付き primary は中身を再帰判定し、それ以外の primary は許可 */
		@Override
		public Boolean visitPrimaryExpression(CPP14Parser.PrimaryExpressionContext ctx) {
			if (ctx.expression() != null) {
				return visit(ctx.expression());
			}
			return true;
		}

		/** 許可: postfix chain（識別子・メンバアクセス・関数呼び出し・添字アクセス） */
		@Override
		public Boolean visitPostfixExpression(CPP14Parser.PostfixExpressionContext ctx) {
			if (ctx.PlusPlus() != null || ctx.MinusMinus() != null) {
				return false;
			}
			if (ctx.Dynamic_cast() != null || ctx.Static_cast() != null || ctx.Reinterpret_cast() != null
					|| ctx.Const_cast() != null) {
				return false;
			}
			if (ctx.simpleTypeSpecifier() != null || ctx.typeNameSpecifier() != null) {
				return false;
			}
			if (ctx.primaryExpression() != null) {
				return visit(ctx.primaryExpression());
			}
			if (ctx.postfixExpression() != null) {
				return visit(ctx.postfixExpression());
			}
			return true;
		}
	}
}
