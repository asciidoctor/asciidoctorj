/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
ruleset {
    ruleset('rulesets/basic.xml') {
        exclude 'EmptyCatchBlock'
        exclude 'EmptyMethod'
    }
    ruleset('rulesets/imports.xml') {
        exclude 'MisorderedStaticImports'
    }
    ruleset('rulesets/naming.xml') {
        exclude 'PropertyName'
        exclude 'MethodName'
        'ClassName' {
            regex = '^[A-Z][a-zA-Z0-9$]*$'
        }
        'FieldName' {
            finalRegex = '^_?[a-z][a-zA-Z0-9]*$'
            staticFinalRegex = '^[A-Z][A-Z_0-9]*$'
        }
        'VariableName' {
            finalRegex = '^_?[a-z][a-zA-Z0-9]*$'
        }
    }
    ruleset('rulesets/unused.xml') {
        exclude 'UnusedMethodParameter'
    }
    ruleset('rulesets/exceptions.xml')
    ruleset('rulesets/logging.xml')
    ruleset('rulesets/braces.xml') {
        exclude 'IfStatementBraces'
    }
    ruleset('rulesets/size.xml')
    ruleset('rulesets/junit.xml') {
        // Does not play well with Spock tests
        exclude 'JUnitPublicNonTestMethod'
    }
    ruleset('rulesets/unnecessary.xml') {
        // UnnecessaryGetter rule does not work correct if there are methods like getRole() and isRole()
        exclude 'UnnecessaryGetter'
        exclude 'UnnecessaryCollectCall'
    }
    ruleset('rulesets/dry.xml') {
        'DuplicateNumberLiteral' {
            ignoreNumbers = '0,1,2'
        }
    }
}
