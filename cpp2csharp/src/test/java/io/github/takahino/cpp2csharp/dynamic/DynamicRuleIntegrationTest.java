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

package io.github.takahino.cpp2csharp.dynamic;

import io.github.takahino.cpp2csharp.converter.ConversionResult;
import io.github.takahino.cpp2csharp.converter.CppToCSharpConverter;
import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader.ThreePassRuleSet;
import io.github.takahino.cpp2csharp.rule.MainPhaseSubPhase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 動的ルール生成の統合テスト。
 *
 * <p>
 * 実際の C++ コードを変換し、トークンストリームから収集した値で 動的生成されたルールが正しく適用されることを検証する。
 * </p>
 */
@DisplayName("動的ルール生成 統合テスト")
class DynamicRuleIntegrationTest {

	private CppToCSharpConverter converter;
	private ConversionRuleLoader ruleLoader;
	private DynamicRuleLoader dynamicLoader;

	@BeforeEach
	void setUp() {
		converter = new CppToCSharpConverter();
		ruleLoader = new ConversionRuleLoader(CppParserFactory.asLexerFactory());
		dynamicLoader = new DynamicRuleLoader(CppParserFactory.asLexerFactory());
	}

	/** drule 文字列から DynamicRuleSpec を1つ生成するヘルパー。 */
	private DynamicRuleSpec spec(String druleContent) {
		return dynamicLoader.parse(druleContent, "test.drule");
	}

	/** ThreePassRuleSet を dynamic specs だけで構築するヘルパー。 */
	private ThreePassRuleSet ruleSetWithDynamic(DynamicRuleSpec... specs) {
		return new ThreePassRuleSet(List.of(), List.of(), List.of(), List.of(), List.of(specs));
	}

	// =========================================================================
	// メソッド定義変換
	// =========================================================================

	@Test
	@DisplayName("void メソッド定義: ClassName::Method → private void Method")
	void voidMethodDefinition() {
		String cpp = "void MyClass::DoSomething() { }";

		DynamicRuleSpec methodSpec = spec("""
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()
				""");

		ConversionResult result = converter.convertSourceThreePass(cpp, ruleSetWithDynamic(methodSpec));

		assertThat(result.getCsCode()).contains("private void DoSomething()");
		assertThat(result.getCsCode()).doesNotContain("MyClass::");
		assertThat(result.getTransformErrors()).isEmpty();
	}

	@Test
	@DisplayName("bool メソッド定義: ClassName::Method → private bool Method")
	void boolMethodDefinition() {
		String cpp = "bool MyClass::IsValid() { return true; }";

		DynamicRuleSpec methodSpec = spec("""
				collect: ABSTRACT_PARAM00 ::
				from: bool COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private bool ABSTRACT_PARAM00()
				""");

		ConversionResult result = converter.convertSourceThreePass(cpp, ruleSetWithDynamic(methodSpec));

		assertThat(result.getCsCode()).contains("private bool IsValid()");
		assertThat(result.getCsCode()).doesNotContain("MyClass::");
	}

	@Test
	@DisplayName("引数ありメソッド定義: void ClassName::Method(int n) → private void Method(int n)")
	void methodDefinitionWithParams() {
		String cpp = "void MyClass::LoadItems(int nCategory) { }";

		DynamicRuleSpec methodSpec = spec("""
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( ABSTRACT_PARAM01 )
				to: private void ABSTRACT_PARAM00(ABSTRACT_PARAM01)
				""");

		ConversionResult result = converter.convertSourceThreePass(cpp, ruleSetWithDynamic(methodSpec));

		assertThat(result.getCsCode()).contains("private void LoadItems(");
		assertThat(result.getCsCode()).contains("int");
		assertThat(result.getCsCode()).doesNotContain("MyClass::");
	}

	@Test
	@DisplayName("複数メソッド: クラス名を1度収集して全メソッドに適用")
	void multipleMethodsShareCollectedClassName() {
		String cpp = """
				void MyClass::MethodA() { }
				void MyClass::MethodB() { }
				""";

		DynamicRuleSpec methodSpec = spec("""
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()
				""");

		ConversionResult result = converter.convertSourceThreePass(cpp, ruleSetWithDynamic(methodSpec));

		assertThat(result.getCsCode()).contains("private void MethodA()");
		assertThat(result.getCsCode()).contains("private void MethodB()");
		assertThat(result.getCsCode()).doesNotContain("MyClass::");
	}

	@Test
	@DisplayName("複数クラスのメソッドをそれぞれ正しく変換する")
	void multipleClassesCollectedAndConverted() {
		String cpp = """
				void ClassA::MethodA() { }
				void ClassB::MethodB() { }
				""";

		DynamicRuleSpec methodSpec = spec("""
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()
				""");

		ConversionResult result = converter.convertSourceThreePass(cpp, ruleSetWithDynamic(methodSpec));

		assertThat(result.getCsCode()).contains("private void MethodA()");
		assertThat(result.getCsCode()).contains("private void MethodB()");
		assertThat(result.getCsCode()).doesNotContain("ClassA::");
		assertThat(result.getCsCode()).doesNotContain("ClassB::");
	}

	@Test
	@DisplayName("コンストラクタ定義（戻り値型なし）は変換しない")
	void constructorNotConverted() {
		String cpp = """
				MyClass::MyClass() { }
				void MyClass::Method() { }
				""";

		DynamicRuleSpec methodSpec = spec("""
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()
				""");

		ConversionResult result = converter.convertSourceThreePass(cpp, ruleSetWithDynamic(methodSpec));

		// コンストラクタは戻り値型なしのため動的ルールにマッチしない
		assertThat(result.getCsCode()).contains("MyClass::MyClass");
		// 通常メソッドは変換される
		assertThat(result.getCsCode()).contains("private void Method()");
	}

	@Test
	@DisplayName("動的ルールなしでは ClassName:: がそのまま残る")
	void withoutDynamicRuleMethodNotConverted() {
		String cpp = "void MyClass::Method() { }";

		ConversionResult result = converter.convertSourceThreePass(cpp,
				new ThreePassRuleSet(List.of(), List.of(), List.of(), List.of(), List.of()));

		assertThat(result.getCsCode()).contains("MyClass::");
		assertThat(result.getCsCode()).doesNotContain("private");
	}

	// =========================================================================
	// COLLECTED が to テンプレートに出現するケース
	// =========================================================================

	@Test
	@DisplayName("to テンプレートの COLLECTED も収集値で置換される（enum 風変換）")
	void collectedInToTemplate() {
		// enum { apple, banana } の banana を収集して (int)banana に変換
		String cpp = "void f() { int x = banana; }";

		DynamicRuleSpec enumSpec = spec("""
				collect: , ABSTRACT_PARAM00 ,
				from: COLLECTED
				to: (int) COLLECTED
				""");

		// collect パターン `, banana ,` にマッチさせるために banana を , で挟む入力
		String cppWithCommas = "enum { apple , banana , cherry }; void f() { int x = banana; }";
		ConversionResult result = converter.convertSourceThreePass(cppWithCommas, ruleSetWithDynamic(enumSpec));

		// banana が収集され、(int) banana に変換される
		assertThat(result.getCsCode()).contains("(int) banana");
	}

	// =========================================================================
	// 動的ルールと静的ルールの共存
	// =========================================================================

	@Test
	@DisplayName("静的 MAIN ルールと動的ルールが同一変換で共存できる")
	void staticAndDynamicRulesCoexist() {
		String cpp = "bool MyClass::IsGood() { return sin(x) > 0; }";

		// 静的ルール: sin → Math.Sin
		var sinRule = ruleLoader
				.loadFromString("from: sin ( ABSTRACT_PARAM00 )\nto: Math.Sin(ABSTRACT_PARAM00)", "sin.rule").get(0);

		DynamicRuleSpec methodSpec = spec("""
				collect: ABSTRACT_PARAM00 ::
				from: bool COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private bool ABSTRACT_PARAM00()
				""");

		ThreePassRuleSet ruleSet = new ThreePassRuleSet(List.of(),
				List.of(new MainPhaseSubPhase("test", List.of(sinRule), true)), List.of(), List.of(),
				List.of(methodSpec));

		ConversionResult result = converter.convertSourceThreePass(cpp, ruleSet);

		assertThat(result.getCsCode()).contains("private bool IsGood()");
		assertThat(result.getCsCode()).contains("Math.Sin");
		assertThat(result.getCsCode()).doesNotContain("MyClass::");
	}
}
