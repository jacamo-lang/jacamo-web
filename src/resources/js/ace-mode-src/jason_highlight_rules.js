/* ***** BEGIN LICENSE BLOCK *****
 * Distributed under the BSD license:
 *
 * Copyright (c) 2012, Ajax.org B.V.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Ajax.org B.V. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL AJAX.ORG B.V. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ***** END LICENSE BLOCK ***** */

define(function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

var JasonHighlightRules = function() {
    // regexp must not have capturing parentheses. Use (?:) instead.
    // regexps are ordered -> the first match is used

    this.$rules = { start:
       [ { include: '#module-directive' },
         { include: '#import-export-directive' },
         { include: '#behaviour-directive' },
         { include: '#record-directive' },
         { include: '#define-directive' },
         { include: '#macro-directive' },
         { include: '#directive' },
         { include: '#function' },
         { include: '#everything-else' } ],
      '#atom':
       [ { token: 'punctuation.definition.symbol.begin.jason',
           regex: '\'',
           push:
            [ { token: 'punctuation.definition.symbol.end.jason',
                regex: '\'',
                next: 'pop' },
              { token:
                 [ 'punctuation.definition.escape.jason',
                   'constant.other.symbol.escape.jason',
                   'punctuation.definition.escape.jason',
                   'constant.other.symbol.escape.jason',
                   'constant.other.symbol.escape.jason' ],
                regex: '(\\\\)(?:([bdefnrstv\\\\\'"])|(\\^)([@-_])|([0-7]{1,3}))' },
              { token: 'invalid.illegal.atom.jason', regex: '\\\\\\^?.?' },
              { defaultToken: 'constant.other.symbol.quoted.single.jason' } ] },
         { token: 'constant.other.symbol.unquoted.jason',
           regex: '[a-z][a-zA-Z\\d@_]*' } ],
      '#behaviour-directive':
       [ { token:
            [ 'meta.directive.behaviour.jason',
              'punctuation.section.directive.begin.jason',
              'meta.directive.behaviour.jason',
              'keyword.control.directive.behaviour.jason',
              'meta.directive.behaviour.jason',
              'punctuation.definition.parameters.begin.jason',
              'meta.directive.behaviour.jason',
              'entity.name.type.class.behaviour.definition.jason',
              'meta.directive.behaviour.jason',
              'punctuation.definition.parameters.end.jason',
              'meta.directive.behaviour.jason',
              'punctuation.section.directive.end.jason' ],
           regex: '^(\\s*)(-)(\\s*)(behaviour)(\\s*)(\\()(\\s*)([a-z][a-zA-Z\\d@_]*)(\\s*)(\\))(\\s*)(\\.)' } ],
      '#binary':
       [ { token: 'punctuation.definition.binary.begin.jason',
           regex: '<<',
           push:
            [ { token: 'punctuation.definition.binary.end.jason',
                regex: '>>',
                next: 'pop' },
              { token:
                 [ 'punctuation.separator.binary.jason',
                   'punctuation.separator.value-size.jason' ],
                regex: '(,)|(:)' },
              { include: '#internal-type-specifiers' },
              { include: '#everything-else' },
              { defaultToken: 'meta.structure.binary.jason' } ] } ],
      '#character':
       [ { token:
            [ 'punctuation.definition.character.jason',
              'punctuation.definition.escape.jason',
              'constant.character.escape.jason',
              'punctuation.definition.escape.jason',
              'constant.character.escape.jason',
              'constant.character.escape.jason' ],
           regex: '(\\$)(\\\\)(?:([bdefnrstv\\\\\'"])|(\\^)([@-_])|([0-7]{1,3}))' },
         { token: 'invalid.illegal.character.jason',
           regex: '\\$\\\\\\^?.?' },
         { token:
            [ 'punctuation.definition.character.jason',
              'constant.character.jason' ],
           regex: '(\\$)(\\S)' },
         { token: 'invalid.illegal.character.jason', regex: '\\$.?' } ],
      '#comment':
       [ { token: 'punctuation.definition.comment.jason',
           regex: '//.*$',
           push_:
            [ { token: 'comment.line.double-slash.jason',
                regex: '$',
                next: 'pop' },
              { defaultToken: 'comment.line.double-slash.jason' } ] }, ],
      '#define-directive':
       [ { token:
            [ 'meta.directive.define.jason',
              'punctuation.section.directive.begin.jason',
              'meta.directive.define.jason',
              'keyword.control.directive.define.jason',
              'meta.directive.define.jason',
              'punctuation.definition.parameters.begin.jason',
              'meta.directive.define.jason',
              'entity.name.function.macro.definition.jason',
              'meta.directive.define.jason',
              'punctuation.separator.parameters.jason' ],
           regex: '^(\\s*)(-)(\\s*)(define)(\\s*)(\\()(\\s*)([a-zA-Z\\d@_]+)(\\s*)(,)',
           push:
            [ { token:
                 [ 'punctuation.definition.parameters.end.jason',
                   'meta.directive.define.jason',
                   'punctuation.section.directive.end.jason' ],
                regex: '(\\))(\\s*)(\\.)',
                next: 'pop' },
              { include: '#everything-else' },
              { defaultToken: 'meta.directive.define.jason' } ] },
         { token: 'meta.directive.define.jason',
           regex: '(?=^\\s*-\\s*define\\s*\\(\\s*[a-zA-Z\\d@_]+\\s*\\()',
           push:
            [ { token:
                 [ 'punctuation.definition.parameters.end.jason',
                   'meta.directive.define.jason',
                   'punctuation.section.directive.end.jason' ],
                regex: '(\\))(\\s*)(\\.)',
                next: 'pop' },
              { token:
                 [ 'text',
                   'punctuation.section.directive.begin.jason',
                   'text',
                   'keyword.control.directive.define.jason',
                   'text',
                   'punctuation.definition.parameters.begin.jason',
                   'text',
                   'entity.name.function.macro.definition.jason',
                   'text',
                   'punctuation.definition.parameters.begin.jason' ],
                regex: '^(\\s*)(-)(\\s*)(define)(\\s*)(\\()(\\s*)([a-zA-Z\\d@_]+)(\\s*)(\\()',
                push:
                 [ { token:
                      [ 'punctuation.definition.parameters.end.jason',
                        'text',
                        'punctuation.separator.parameters.jason' ],
                     regex: '(\\))(\\s*)(,)',
                     next: 'pop' },
                   { token: 'punctuation.separator.parameters.jason', regex: ',' },
                   { include: '#everything-else' } ] },
              { token: 'punctuation.separator.define.jason',
                regex: '\\|\\||\\||:|;|,|\\.|->' },
              { include: '#everything-else' },
              { defaultToken: 'meta.directive.define.jason' } ] } ],
      '#directive':
       [ { token:
            [ 'meta.directive.jason',
              'punctuation.section.directive.begin.jason',
              'meta.directive.jason',
              'keyword.control.directive.jason',
              'meta.directive.jason',
              'punctuation.definition.parameters.begin.jason' ],
           regex: '^(\\s*)(-)(\\s*)([a-z][a-zA-Z\\d@_]*)(\\s*)(\\(?)',
           push:
            [ { token:
                 [ 'punctuation.definition.parameters.end.jason',
                   'meta.directive.jason',
                   'punctuation.section.directive.end.jason' ],
                regex: '(\\)?)(\\s*)(\\.)',
                next: 'pop' },
              { include: '#everything-else' },
              { defaultToken: 'meta.directive.jason' } ] },
         { token:
            [ 'meta.directive.jason',
              'punctuation.section.directive.begin.jason',
              'meta.directive.jason',
              'keyword.control.directive.jason',
              'meta.directive.jason',
              'punctuation.section.directive.end.jason' ],
           regex: '^(\\s*)(-)(\\s*)([a-z][a-zA-Z\\d@_]*)(\\s*)(\\.)' } ],
      '#everything-else':
       [ { include: '#comment' },
         { include: '#multilinecomment' },
         { include: '#record-usage' },
         { include: '#macro-usage' },
         { include: '#expression' },
         { include: '#keyword' },
         { include: '#textual-operator' },
         { include: '#function-call' },
         { include: '#tuple' },
         { include: '#list' },
         { include: '#binary' },
         { include: '#parenthesized-expression' },
         { include: '#character' },
         { include: '#number' },
         { include: '#atom' },
         { include: '#string' },
         { include: '#symbolic-operator' },
         { include: '#variable' } ],
      '#expression':
       [ { token: 'keyword.control.if.jason',
           regex: '\\bif\\b',
           push:
            [ { token: 'keyword.control.end.jason',
                regex: '\\bend\\b',
                next: 'pop' },
              { include: '#internal-expression-punctuation' },
              { include: '#everything-else' },
              { defaultToken: 'meta.expression.if.jason' } ] },
         { token: 'keyword.control.case.jason',
           regex: '\\bcase\\b',
           push:
            [ { token: 'keyword.control.end.jason',
                regex: '\\bend\\b',
                next: 'pop' },
              { include: '#internal-expression-punctuation' },
              { include: '#everything-else' },
              { defaultToken: 'meta.expression.case.jason' } ] },
         { token: 'keyword.control.receive.jason',
           regex: '\\breceive\\b',
           push:
            [ { token: 'keyword.control.end.jason',
                regex: '\\bend\\b',
                next: 'pop' },
              { include: '#internal-expression-punctuation' },
              { include: '#everything-else' },
              { defaultToken: 'meta.expression.receive.jason' } ] },
         { token:
            [ 'keyword.control.fun.jason',
              'text',
              'entity.name.type.class.module.jason',
              'text',
              'punctuation.separator.module-function.jason',
              'text',
              'entity.name.function.jason',
              'text',
              'punctuation.separator.function-arity.jason' ],
           regex: '\\b(fun)(\\s*)(?:([a-z][a-zA-Z\\d@_]*)(\\s*)(:)(\\s*))?([a-z][a-zA-Z\\d@_]*)(\\s*)(/)' },
         { token: 'keyword.control.fun.jason',
           regex: '\\bfun\\b',
           push:
            [ { token: 'keyword.control.end.jason',
                regex: '\\bend\\b',
                next: 'pop' },
              { token: 'text',
                regex: '(?=\\()',
                push:
                 [ { token: 'punctuation.separator.clauses.jason',
                     regex: ';|(?=\\bend\\b)',
                     next: 'pop' },
                   { include: '#internal-function-parts' } ] },
              { include: '#everything-else' },
              { defaultToken: 'meta.expression.fun.jason' } ] },
         { token: 'keyword.control.try.jason',
           regex: '\\btry\\b',
           push:
            [ { token: 'keyword.control.end.jason',
                regex: '\\bend\\b',
                next: 'pop' },
              { include: '#internal-expression-punctuation' },
              { include: '#everything-else' },
              { defaultToken: 'meta.expression.try.jason' } ] },
         { token: 'keyword.control.begin.jason',
           regex: '\\bbegin\\b',
           push:
            [ { token: 'keyword.control.end.jason',
                regex: '\\bend\\b',
                next: 'pop' },
              { include: '#internal-expression-punctuation' },
              { include: '#everything-else' },
              { defaultToken: 'meta.expression.begin.jason' } ] },
         { token: 'keyword.control.query.jason',
           regex: '\\bquery\\b',
           push:
            [ { token: 'keyword.control.end.jason',
                regex: '\\bend\\b',
                next: 'pop' },
              { include: '#everything-else' },
              { defaultToken: 'meta.expression.query.jason' } ] } ],
      '#function':
       [ { token:
            [ 'meta.function.jason',
              'entity.name.function.definition.jason',
              'meta.function.jason' ],
           regex: '^(\\s*)([a-z][a-zA-Z\\d@_]*|\'[^\']*\')(\\s*)(?=\\()',
           push:
            [ { token: 'punctuation.terminator.function.jason',
                regex: '\\.',
                next: 'pop' },
              { token: [ 'text', 'entity.name.function.jason', 'text' ],
                regex: '^(\\s*)([a-z][a-zA-Z\\d@_]*|\'[^\']*\')(\\s*)(?=\\()' },
              { token: 'text',
                regex: '(?=\\()',
                push:
                 [ { token: 'punctuation.separator.clauses.jason',
                     regex: ';|(?=\\.)',
                     next: 'pop' },
                   { include: '#parenthesized-expression' },
                   { include: '#internal-function-parts' } ] },
              { include: '#everything-else' },
              { defaultToken: 'meta.function.jason' } ] } ],
      '#function-call':
       [ { token: 'meta.function-call.jason',
           regex: '(?=(?:[a-z][a-zA-Z\\d@_]*|\'[^\']*\')\\s*(?:\\(|:\\s*(?:[a-z][a-zA-Z\\d@_]*|\'[^\']*\')\\s*\\())',
           push:
            [ { token: 'punctuation.definition.parameters.end.jason',
                regex: '\\)',
                next: 'pop' },
              { token:
                 [ 'entity.name.type.class.module.jason',
                   'text',
                   'punctuation.separator.module-function.jason',
                   'text',
                   'entity.name.function.guard.jason',
                   'text',
                   'punctuation.definition.parameters.begin.jason' ],
                regex: '(?:(jason)(\\s*)(:)(\\s*))?(is_atom|is_binary|is_constant|is_float|is_function|is_integer|is_list|is_number|is_pid|is_port|is_reference|is_tuple|is_record|abs|element|hd|length|node|round|self|size|tl|trunc)(\\s*)(\\()',
                push:
                 [ { token: 'text', regex: '(?=\\))', next: 'pop' },
                   { token: 'punctuation.separator.parameters.jason', regex: ',' },
                   { include: '#everything-else' } ] },
              { token:
                 [ 'entity.name.type.class.module.jason',
                   'text',
                   'punctuation.separator.module-function.jason',
                   'text',
                   'entity.name.function.jason',
                   'text',
                   'punctuation.definition.parameters.begin.jason' ],
                regex: '(?:([a-z][a-zA-Z\\d@_]*|\'[^\']*\')(\\s*)(:)(\\s*))?([a-z][a-zA-Z\\d@_]*|\'[^\']*\')(\\s*)(\\()',
                push:
                 [ { token: 'text', regex: '(?=\\))', next: 'pop' },
                   { token: 'punctuation.separator.parameters.jason', regex: ',' },
                   { include: '#everything-else' } ] },
              { defaultToken: 'meta.function-call.jason' } ] } ],
      '#import-export-directive':
       [ { token:
            [ 'meta.directive.import.jason',
              'punctuation.section.directive.begin.jason',
              'meta.directive.import.jason',
              'keyword.control.directive.import.jason',
              'meta.directive.import.jason',
              'punctuation.definition.parameters.begin.jason',
              'meta.directive.import.jason',
              'entity.name.type.class.module.jason',
              'meta.directive.import.jason',
              'punctuation.separator.parameters.jason' ],
           regex: '^(\\s*)(-)(\\s*)(import)(\\s*)(\\()(\\s*)([a-z][a-zA-Z\\d@_]*|\'[^\']*\')(\\s*)(,)',
           push:
            [ { token:
                 [ 'punctuation.definition.parameters.end.jason',
                   'meta.directive.import.jason',
                   'punctuation.section.directive.end.jason' ],
                regex: '(\\))(\\s*)(\\.)',
                next: 'pop' },
              { include: '#internal-function-list' },
              { defaultToken: 'meta.directive.import.jason' } ] },
         { token:
            [ 'meta.directive.export.jason',
              'punctuation.section.directive.begin.jason',
              'meta.directive.export.jason',
              'keyword.control.directive.export.jason',
              'meta.directive.export.jason',
              'punctuation.definition.parameters.begin.jason' ],
           regex: '^(\\s*)(-)(\\s*)(export)(\\s*)(\\()',
           push:
            [ { token:
                 [ 'punctuation.definition.parameters.end.jason',
                   'meta.directive.export.jason',
                   'punctuation.section.directive.end.jason' ],
                regex: '(\\))(\\s*)(\\.)',
                next: 'pop' },
              { include: '#internal-function-list' },
              { defaultToken: 'meta.directive.export.jason' } ] } ],
      '#internal-expression-punctuation':
       [ { token:
            [ 'punctuation.separator.clause-head-body.jason',
              'punctuation.separator.clauses.jason',
              'punctuation.separator.expressions.jason' ],
           regex: '(->)|(;)|(,)' } ],
      '#internal-function-list':
       [ { token: 'punctuation.definition.list.begin.jason',
           regex: '\\[',
           push:
            [ { token: 'punctuation.definition.list.end.jason',
                regex: '\\]',
                next: 'pop' },
              { token:
                 [ 'entity.name.function.jason',
                   'text',
                   'punctuation.separator.function-arity.jason' ],
                regex: '([a-z][a-zA-Z\\d@_]*|\'[^\']*\')(\\s*)(/)',
                push:
                 [ { token: 'punctuation.separator.list.jason',
                     regex: ',|(?=\\])',
                     next: 'pop' },
                   { include: '#everything-else' } ] },
              { include: '#everything-else' },
              { defaultToken: 'meta.structure.list.function.jason' } ] } ],
      '#internal-function-parts':
       [ { token: 'text',
           regex: '(?=\\()',
           push:
            [ { token: 'punctuation.separator.clause-head-body.jason',
                regex: '->',
                next: 'pop' },
              { token: 'punctuation.definition.parameters.begin.jason',
                regex: '\\(',
                push:
                 [ { token: 'punctuation.definition.parameters.end.jason',
                     regex: '\\)',
                     next: 'pop' },
                   { token: 'punctuation.separator.parameters.jason', regex: ',' },
                   { include: '#everything-else' } ] },
              { token: 'punctuation.separator.guards.jason', regex: ',|;' },
              { include: '#everything-else' } ] },
         { token: 'punctuation.separator.expressions.jason',
           regex: ',' },
         { include: '#everything-else' } ],
      '#internal-record-body':
       [ { token: 'punctuation.definition.class.record.begin.jason',
           regex: '\\{',
           push:
            [ { token: 'meta.structure.record.jason',
                regex: '(?=\\})',
                next: 'pop' },
              { token:
                 [ 'variable.other.field.jason',
                   'variable.language.omitted.field.jason',
                   'text',
                   'keyword.operator.assignment.jason' ],
                regex: '(?:([a-z][a-zA-Z\\d@_]*|\'[^\']*\')|(_))(\\s*)(=|::)',
                push:
                 [ { token: 'punctuation.separator.class.record.jason',
                     regex: ',|(?=\\})',
                     next: 'pop' },
                   { include: '#everything-else' } ] },
              { token:
                 [ 'variable.other.field.jason',
                   'text',
                   'punctuation.separator.class.record.jason' ],
                regex: '([a-z][a-zA-Z\\d@_]*|\'[^\']*\')(\\s*)((?:,)?)' },
              { include: '#everything-else' },
              { defaultToken: 'meta.structure.record.jason' } ] } ],
      '#internal-type-specifiers':
       [ { token: 'punctuation.separator.value-type.jason',
           regex: '/',
           push:
            [ { token: 'text', regex: '(?=,|:|>>)', next: 'pop' },
              { token:
                 [ 'storage.type.jason',
                   'storage.modifier.signedness.jason',
                   'storage.modifier.endianness.jason',
                   'storage.modifier.unit.jason',
                   'punctuation.separator.type-specifiers.jason' ],
                regex: '(integer|float|binary|bytes|bitstring|bits)|(signed|unsigned)|(big|little|native)|(unit)|(-)' } ] } ],
      '#keyword':
       [ { token: 'keyword.control.jason',
           regex: '\\b(?:after|begin|case|catch|cond|end|fun|if|let|of|query|try|receive|when)\\b' } ],
      '#list':
       [ { token: 'punctuation.definition.list.begin.jason',
           regex: '\\[',
           push:
            [ { token: 'punctuation.definition.list.end.jason',
                regex: '\\]',
                next: 'pop' },
              { token: 'punctuation.separator.list.jason',
                regex: '\\||\\|\\||,' },
              { include: '#everything-else' },
              { defaultToken: 'meta.structure.list.jason' } ] } ],
      '#macro-directive':
       [ { token:
            [ 'meta.directive.ifdef.jason',
              'punctuation.section.directive.begin.jason',
              'meta.directive.ifdef.jason',
              'keyword.control.directive.ifdef.jason',
              'meta.directive.ifdef.jason',
              'punctuation.definition.parameters.begin.jason',
              'meta.directive.ifdef.jason',
              'entity.name.function.macro.jason',
              'meta.directive.ifdef.jason',
              'punctuation.definition.parameters.end.jason',
              'meta.directive.ifdef.jason',
              'punctuation.section.directive.end.jason' ],
           regex: '^(\\s*)(-)(\\s*)(ifdef)(\\s*)(\\()(\\s*)([a-zA-Z\\d@_]+)(\\s*)(\\))(\\s*)(\\.)' },
         { token:
            [ 'meta.directive.ifndef.jason',
              'punctuation.section.directive.begin.jason',
              'meta.directive.ifndef.jason',
              'keyword.control.directive.ifndef.jason',
              'meta.directive.ifndef.jason',
              'punctuation.definition.parameters.begin.jason',
              'meta.directive.ifndef.jason',
              'entity.name.function.macro.jason',
              'meta.directive.ifndef.jason',
              'punctuation.definition.parameters.end.jason',
              'meta.directive.ifndef.jason',
              'punctuation.section.directive.end.jason' ],
           regex: '^(\\s*)(-)(\\s*)(ifndef)(\\s*)(\\()(\\s*)([a-zA-Z\\d@_]+)(\\s*)(\\))(\\s*)(\\.)' },
         { token:
            [ 'meta.directive.undef.jason',
              'punctuation.section.directive.begin.jason',
              'meta.directive.undef.jason',
              'keyword.control.directive.undef.jason',
              'meta.directive.undef.jason',
              'punctuation.definition.parameters.begin.jason',
              'meta.directive.undef.jason',
              'entity.name.function.macro.jason',
              'meta.directive.undef.jason',
              'punctuation.definition.parameters.end.jason',
              'meta.directive.undef.jason',
              'punctuation.section.directive.end.jason' ],
           regex: '^(\\s*)(-)(\\s*)(undef)(\\s*)(\\()(\\s*)([a-zA-Z\\d@_]+)(\\s*)(\\))(\\s*)(\\.)' } ],
      '#macro-usage':
       [ { token:
            [ 'keyword.operator.macro.jason',
              'meta.macro-usage.jason',
              'entity.name.function.macro.jason' ],
           regex: '(\\?\\??)(\\s*)([a-zA-Z\\d@_]+)' } ],
      '#module-directive':
       [ { token:
            [ 'meta.directive.module.jason',
              'punctuation.section.directive.begin.jason',
              'meta.directive.module.jason',
              'keyword.control.directive.module.jason',
              'meta.directive.module.jason',
              'punctuation.definition.parameters.begin.jason',
              'meta.directive.module.jason',
              'entity.name.type.class.module.definition.jason',
              'meta.directive.module.jason',
              'punctuation.definition.parameters.end.jason',
              'meta.directive.module.jason',
              'punctuation.section.directive.end.jason' ],
           regex: '^(\\s*)(-)(\\s*)(module)(\\s*)(\\()(\\s*)([a-z][a-zA-Z\\d@_]*)(\\s*)(\\))(\\s*)(\\.)' } ],
      '#number':
       [ { token: 'text',
           regex: '(?=\\d)',
           push:
            [ { token: 'text', regex: '(?!\\d)', next: 'pop' },
              { token:
                 [ 'constant.numeric.float.jason',
                   'punctuation.separator.integer-float.jason',
                   'constant.numeric.float.jason',
                   'punctuation.separator.float-exponent.jason' ],
                regex: '(\\d+)(\\.)(\\d+)((?:[eE][\\+\\-]?\\d+)?)' },
              { token:
                 [ 'constant.numeric.integer.binary.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.binary.jason' ],
                regex: '(2)(#)([0-1]+)' },
              { token:
                 [ 'constant.numeric.integer.base-3.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-3.jason' ],
                regex: '(3)(#)([0-2]+)' },
              { token:
                 [ 'constant.numeric.integer.base-4.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-4.jason' ],
                regex: '(4)(#)([0-3]+)' },
              { token:
                 [ 'constant.numeric.integer.base-5.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-5.jason' ],
                regex: '(5)(#)([0-4]+)' },
              { token:
                 [ 'constant.numeric.integer.base-6.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-6.jason' ],
                regex: '(6)(#)([0-5]+)' },
              { token:
                 [ 'constant.numeric.integer.base-7.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-7.jason' ],
                regex: '(7)(#)([0-6]+)' },
              { token:
                 [ 'constant.numeric.integer.octal.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.octal.jason' ],
                regex: '(8)(#)([0-7]+)' },
              { token:
                 [ 'constant.numeric.integer.base-9.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-9.jason' ],
                regex: '(9)(#)([0-8]+)' },
              { token:
                 [ 'constant.numeric.integer.decimal.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.decimal.jason' ],
                regex: '(10)(#)(\\d+)' },
              { token:
                 [ 'constant.numeric.integer.base-11.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-11.jason' ],
                regex: '(11)(#)([\\daA]+)' },
              { token:
                 [ 'constant.numeric.integer.base-12.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-12.jason' ],
                regex: '(12)(#)([\\da-bA-B]+)' },
              { token:
                 [ 'constant.numeric.integer.base-13.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-13.jason' ],
                regex: '(13)(#)([\\da-cA-C]+)' },
              { token:
                 [ 'constant.numeric.integer.base-14.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-14.jason' ],
                regex: '(14)(#)([\\da-dA-D]+)' },
              { token:
                 [ 'constant.numeric.integer.base-15.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-15.jason' ],
                regex: '(15)(#)([\\da-eA-E]+)' },
              { token:
                 [ 'constant.numeric.integer.hexadecimal.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.hexadecimal.jason' ],
                regex: '(16)(#)([\\da-fA-F]+)' },
              { token:
                 [ 'constant.numeric.integer.base-17.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-17.jason' ],
                regex: '(17)(#)([\\da-gA-G]+)' },
              { token:
                 [ 'constant.numeric.integer.base-18.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-18.jason' ],
                regex: '(18)(#)([\\da-hA-H]+)' },
              { token:
                 [ 'constant.numeric.integer.base-19.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-19.jason' ],
                regex: '(19)(#)([\\da-iA-I]+)' },
              { token:
                 [ 'constant.numeric.integer.base-20.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-20.jason' ],
                regex: '(20)(#)([\\da-jA-J]+)' },
              { token:
                 [ 'constant.numeric.integer.base-21.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-21.jason' ],
                regex: '(21)(#)([\\da-kA-K]+)' },
              { token:
                 [ 'constant.numeric.integer.base-22.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-22.jason' ],
                regex: '(22)(#)([\\da-lA-L]+)' },
              { token:
                 [ 'constant.numeric.integer.base-23.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-23.jason' ],
                regex: '(23)(#)([\\da-mA-M]+)' },
              { token:
                 [ 'constant.numeric.integer.base-24.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-24.jason' ],
                regex: '(24)(#)([\\da-nA-N]+)' },
              { token:
                 [ 'constant.numeric.integer.base-25.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-25.jason' ],
                regex: '(25)(#)([\\da-oA-O]+)' },
              { token:
                 [ 'constant.numeric.integer.base-26.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-26.jason' ],
                regex: '(26)(#)([\\da-pA-P]+)' },
              { token:
                 [ 'constant.numeric.integer.base-27.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-27.jason' ],
                regex: '(27)(#)([\\da-qA-Q]+)' },
              { token:
                 [ 'constant.numeric.integer.base-28.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-28.jason' ],
                regex: '(28)(#)([\\da-rA-R]+)' },
              { token:
                 [ 'constant.numeric.integer.base-29.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-29.jason' ],
                regex: '(29)(#)([\\da-sA-S]+)' },
              { token:
                 [ 'constant.numeric.integer.base-30.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-30.jason' ],
                regex: '(30)(#)([\\da-tA-T]+)' },
              { token:
                 [ 'constant.numeric.integer.base-31.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-31.jason' ],
                regex: '(31)(#)([\\da-uA-U]+)' },
              { token:
                 [ 'constant.numeric.integer.base-32.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-32.jason' ],
                regex: '(32)(#)([\\da-vA-V]+)' },
              { token:
                 [ 'constant.numeric.integer.base-33.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-33.jason' ],
                regex: '(33)(#)([\\da-wA-W]+)' },
              { token:
                 [ 'constant.numeric.integer.base-34.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-34.jason' ],
                regex: '(34)(#)([\\da-xA-X]+)' },
              { token:
                 [ 'constant.numeric.integer.base-35.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-35.jason' ],
                regex: '(35)(#)([\\da-yA-Y]+)' },
              { token:
                 [ 'constant.numeric.integer.base-36.jason',
                   'punctuation.separator.base-integer.jason',
                   'constant.numeric.integer.base-36.jason' ],
                regex: '(36)(#)([\\da-zA-Z]+)' },
              { token: 'invalid.illegal.integer.jason',
                regex: '\\d+#[\\da-zA-Z]+' },
              { token: 'constant.numeric.integer.decimal.jason',
                regex: '\\d+' } ] } ],
      '#parenthesized-expression':
       [ { token: 'punctuation.section.expression.begin.jason',
           regex: '\\(',
           push:
            [ { token: 'punctuation.section.expression.end.jason',
                regex: '\\)',
                next: 'pop' },
              { include: '#everything-else' },
              { defaultToken: 'meta.expression.parenthesized' } ] } ],
      '#record-directive':
       [ { token:
            [ 'meta.directive.record.jason',
              'punctuation.section.directive.begin.jason',
              'meta.directive.record.jason',
              'keyword.control.directive.import.jason',
              'meta.directive.record.jason',
              'punctuation.definition.parameters.begin.jason',
              'meta.directive.record.jason',
              'entity.name.type.class.record.definition.jason',
              'meta.directive.record.jason',
              'punctuation.separator.parameters.jason' ],
           regex: '^(\\s*)(-)(\\s*)(record)(\\s*)(\\()(\\s*)([a-z][a-zA-Z\\d@_]*|\'[^\']*\')(\\s*)(,)',
           push:
            [ { token:
                 [ 'punctuation.definition.class.record.end.jason',
                   'meta.directive.record.jason',
                   'punctuation.definition.parameters.end.jason',
                   'meta.directive.record.jason',
                   'punctuation.section.directive.end.jason' ],
                regex: '(\\})(\\s*)(\\))(\\s*)(\\.)',
                next: 'pop' },
              { include: '#internal-record-body' },
              { defaultToken: 'meta.directive.record.jason' } ] } ],
      '#record-usage':
       [ { token:
            [ 'keyword.operator.record.jason',
              'meta.record-usage.jason',
              'entity.name.type.class.record.jason',
              'meta.record-usage.jason',
              'punctuation.separator.record-field.jason',
              'meta.record-usage.jason',
              'variable.other.field.jason' ],
           regex: '(#)(\\s*)([a-z][a-zA-Z\\d@_]*|\'[^\']*\')(\\s*)(\\.)(\\s*)([a-z][a-zA-Z\\d@_]*|\'[^\']*\')' },
         { token:
            [ 'keyword.operator.record.jason',
              'meta.record-usage.jason',
              'entity.name.type.class.record.jason' ],
           regex: '(#)(\\s*)([a-z][a-zA-Z\\d@_]*|\'[^\']*\')',
           push:
            [ { token: 'punctuation.definition.class.record.end.jason',
                regex: '\\}',
                next: 'pop' },
              { include: '#internal-record-body' },
              { defaultToken: 'meta.record-usage.jason' } ] } ],
      '#string':
       [ { token: 'punctuation.definition.string.begin.jason',
           regex: '"',
           push:
            [ { token: 'punctuation.definition.string.end.jason',
                regex: '"',
                next: 'pop' },
              { token:
                 [ 'punctuation.definition.escape.jason',
                   'constant.character.escape.jason',
                   'punctuation.definition.escape.jason',
                   'constant.character.escape.jason',
                   'constant.character.escape.jason' ],
                regex: '(\\\\)(?:([bdefnrstv\\\\\'"])|(\\^)([@-_])|([0-7]{1,3}))' },
              { token: 'invalid.illegal.string.jason', regex: '\\\\\\^?.?' },
              { token:
                 [ 'punctuation.definition.placeholder.jason',
                   'punctuation.separator.placeholder-parts.jason',
                   'constant.other.placeholder.jason',
                   'punctuation.separator.placeholder-parts.jason',
                   'punctuation.separator.placeholder-parts.jason',
                   'constant.other.placeholder.jason',
                   'punctuation.separator.placeholder-parts.jason',
                   'punctuation.separator.placeholder-parts.jason',
                   'punctuation.separator.placeholder-parts.jason',
                   'constant.other.placeholder.jason',
                   'constant.other.placeholder.jason' ],
                regex: '(~)(?:((?:\\-)?)(\\d+)|(\\*))?(?:(\\.)(?:(\\d+)|(\\*)))?(?:(\\.)(?:(\\*)|(.)))?([~cfegswpWPBX#bx\\+ni])' },
              { token:
                 [ 'punctuation.definition.placeholder.jason',
                   'punctuation.separator.placeholder-parts.jason',
                   'constant.other.placeholder.jason',
                   'constant.other.placeholder.jason' ],
                regex: '(~)((?:\\*)?)((?:\\d+)?)([~du\\-#fsacl])' },
              { token: 'invalid.illegal.string.jason', regex: '~.?' },
              { defaultToken: 'string.quoted.double.jason' } ] } ],
      '#symbolic-operator':
       [ { token: 'keyword.operator.symbolic.jason',
           regex: '\\+\\+|\\+|--|-|\\*|/=|/|=/=|=:=|==|=<|=|<-|<|>=|>|!|::' } ],
      '#textual-operator':
       [ { token: 'keyword.operator.textual.jason',
           regex: '\\b(?:andalso|band|and|bxor|xor|bor|orelse|or|bnot|not|bsl|bsr|div|rem)\\b' } ],
      '#tuple':
       [ { token: 'punctuation.definition.tuple.begin.jason',
           regex: '\\{',
           push:
            [ { token: 'punctuation.definition.tuple.end.jason',
                regex: '\\}',
                next: 'pop' },
              { token: 'punctuation.separator.tuple.jason', regex: ',' },
              { include: '#everything-else' },
              { defaultToken: 'meta.structure.tuple.jason' } ] } ],
      '#variable':
       [ { token: [ 'variable.other.jason', 'variable.language.omitted.jason' ],
           regex: '(_[a-zA-Z\\d@_]+|[A-Z][a-zA-Z\\d@_]*)|(_)' } ],
      '#multilinecomment' :
       [ { token : "comment.begin.jason",
           regex : "\\/\\*",
           push:
           [ { token: 'comment.end.jason',
               regex: '\\*\\/',
               next: 'pop' },
             { defaultToken : "comment.jason" } ] } ]
       };

    this.normalizeRules();
};

JasonHighlightRules.metaData = { comment: 'The recognition of function definitions and compiler directives (such as module, record and macro definitions) requires that each of the aforementioned constructs must be the first string inside a line (except for whitespace).  Also, the function/module/record/macro names must be given unquoted.  -- desp',
      fileTypes: [ 'asl', 'jcm' ],
      keyEquivalent: '^~E',
      name: 'Jason',
      scopeName: 'source.jason' };


oop.inherits(JasonHighlightRules, TextHighlightRules);

exports.JasonHighlightRules = JasonHighlightRules;
});
