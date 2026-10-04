# Analizador Sintáctico Descendente Recursivo - MiniLenguaje (Java)

Aplicación gráfica interactiva desarrollada en **Java Swing** que implementa un **Analizador Léxico** (Scanner) y un **Parser Sintáctico Descendente Recursivo** para evaluar la sintaxis de un mini-lenguaje de programación.

---

## 📌 Cumplimiento de Requisitos
- [x] **Aplicación Gráfica (GUI)**: Desarrollada con Java Swing (`JFrame`), sin uso de entrada/salida por consola.
- [x] **Código Fuente y Ejecutable**: Incluye código fuente `.java`, ejecutable empaquetado `.jar` y ejecutable directo `.bat`.
- [x] **Documentación del Lenguaje**: Especificación formal BNF y casos de prueba detallados.

---

## 📐 Documentación del Lenguaje y Gramática (BNF)

El mini-lenguaje soporta declaraciones de asignación y expresiones aritméticas respetando la jerarquía de operadores y paréntesis:

```text
<Programa>    ::= <Sentencia>*
<Sentencia>   ::= ID '=' <Expresion> ';'
<Expresion>   ::= <Termino> ( ('+' | '-') <Termino> )*
<Termino>     ::= <Factor> ( ('*' | '/') <Factor> )*
<Factor>      ::= NUMERO | ID | '(' <Expresion> ')'