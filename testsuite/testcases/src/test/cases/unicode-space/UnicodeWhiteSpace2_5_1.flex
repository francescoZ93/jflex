%%

%unicode 5.1
%public
%class UnicodeWhiteSpace2_5_1

%type int
%standalone

%include ../../resources/common-unicode-all-enumerated-property-java

%%

<<EOF>> { printOutput(); return 1; }
\s { setCurCharPropertyValue("Whitespace"); }
[^\s] { setCurCharPropertyValue("Not-Whitespace"); }
