# How to use

Compile with maven ``mvn clean package`` or download from [github](https://github.com/scrat98/data-compressor/releases) and execute the jar  
``java -jar data-compressor.jar <encode | decode> <input file> <output file>``

# Project structure
Есть интерфейс ``Compressor``, которые реализуют соответствующие алгоритмы. Все алгоритмы разбиты по пакетам с их названием. Каждый компрессор проверяется на наборе файлов и случайном наборе байт, что он может закодировать и декодировать без ошибок.  
Класс ``DataCompressor`` включает в себя цепочку компрессоров и поочередно их вызывает.

# Algorithm explanation

Сначала был реализован алгоритм Адаптивного Арифметического кодирования. Затем "компрессор" улучшался. В итоге, вся цепочка компрессора состоит из: ``RLE <-> BWT <-> MTF <-> RLE <-> A0``.
Стоит заметить, что файл не выгружается целиком в память(во избежание проблем) и каждый следующий алгоритм читает временный файл предыдущего алгоритма. Т.е. данный архиватор является полностью адаптивным.  
С итоговым сравнением эффективности разных цепочек алгоритмов вы можете ознакомиться [здесь](#overall-result). Ниже будут описаны краткие детали каждого из алгоритмов

## Arithmetic coding
Энтропийный алгоритм сжатия, который основан на частоте символов. За основу была взята статья Witten'а. Данный метод основан на целочисленной реализации. 
Также в данной реализации не передается размер файла, а вводиться дополнительный символ окончания потока(EOF), т.е. расширяется алфавит и число интервалов, на которые делится отрезок.
Тем самым делая алгоритм адаптивным. Частота символов вычисляется динамически по их приходу и увеличивает соответствующий счетчик.

Источники:
- Witten, I.H., Neal, R., and Cleary, J.G., (1987b) “Arithmetic coding for data compression,” Communications of the Association for Computing Machinery,30 (6) 520-540, June.
- https://neerc.ifmo.ru/wiki/index.php?title=%D0%90%D1%80%D0%B8%D1%84%D0%BC%D0%B5%D1%82%D0%B8%D1%87%D0%B5%D1%81%D0%BA%D0%BE%D0%B5_%D0%BA%D0%BE%D0%B4%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5
- https://habr.com/ru/post/130531/

## BWT

Данное преобразование позволяет преобразовать исходный поток в поток повторяющихся подстрок.  
Кодирование и декодирование происходит на блоке байт равному 200кБайт  

*Кодирование*:  
В данной реализации добавляется искусственно дополнительный символ конца строки(EOF), который является лексикографически большим любого символа.
Это позволяет использовать префиксную сортировку, основанную на индексах. В данной реализации использована стандартная сортировка TimSort в Java, которая работает в среднем за O(n), а в худшем за O(n*log n).  

Таким образом закодированная строка выглядит следующим образом ``[data] | first_index | eof_index``. 
firstIndex - это номер исходной строки, eofIndex - индекс символа конца строки. 
Тем самым мы добавляем дополнительно 2 * 4(на указатели) + 1(на EOF) = 9 байт на каждый блок данных, но это дает нам возможность кодировать за линейное время и не считывать весь файл в память.

*Декодирование*:  
Используется метод вектора обратного преобразования. Работает за линейное время

Источники:
- https://www.youtube.com/watch?v=4n7NPk5lwbI
- https://www.quora.com/Algorithms/How-can-I-optimize-burrows-wheeler-transform-and-inverse-transform-to-work-in-O-n-time-O-n-space
- https://neerc.ifmo.ru/wiki/index.php?title=%D0%9F%D1%80%D0%B5%D0%BE%D0%B1%D1%80%D0%B0%D0%B7%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5_%D0%91%D0%B0%D1%80%D1%80%D0%BE%D1%83%D0%B7%D0%B0-%D0%A3%D0%B8%D0%BB%D0%B5%D1%80%D0%B0
- https://compression.ru/book/pdf/compression_methods_part1_5-7.pdf
- http://mf.grsu.by/UchProc/livak/po/comprsite/theory_bwt.html

## MTF

Алгоритм позволяет после BWT превратить поток в поток повторяющихся байт. Этот подход используется в bzip2. Реализован без каких либо модификаций.

Источники:
- http://neerc.ifmo.ru/wiki/index.php?title=%D0%9F%D1%80%D0%B5%D0%BE%D0%B1%D1%80%D0%B0%D0%B7%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5_MTF

## RLE
Поточный алгоритм сжатия. Можно сказать, является наивной реализацией алгоритмов семейства LZ*. Он позволяет сжимать повторяющиеся серии символов. Под максимальную длину серии был взят размер 1 байта.
Было рассмотрено два варианта реализации:
1) В первом байте хранить длину и если она отрицательная, то значит следующие n символов не являются серией и мы их просто считываем, если положительная, то будет следовать n повторений следующего символа.
Была сделана маленькая оптимизация, что длины повторяющихся цепочек хранились от 2 до 129, так как минимальная цепочка имеет длину 2.  
``ABCABCABCDDDFFFFFF -> -9ABCABCABC3D6F``

2) Мы пишем символы и если встретили повторяющиеся, то после них пишем длину еще повторений этих же символов. ``ABCAAA -> ABCAA1``
Это плохо работает только со строками, где много серий длины два, тогда мы каждый раз увеличиваем длину файла на 1 байт(``AA -> AA0``), но это нивелируется следующими алгоритмами BWT и MTF.  

Второй вариант реализации оказался более эффективным.

Этот алгоритм применяется до BWT, чтобы сделать более быструю сортировку впоследствии. И после MTF, так как образуется много нулей после него.

Источники:
- https://habr.com/ru/post/141827/
- https://ru.wikipedia.org/wiki/%D0%9A%D0%BE%D0%B4%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5_%D0%B4%D0%BB%D0%B8%D0%BD_%D1%81%D0%B5%D1%80%D0%B8%D0%B9

## TODO
Заменить алгоритм RLE на LZW/LZ77, что даст прирост в качестве сжатия.
Можно в алгоритме BWT схлопывать тройки букв в 32bit число. или 8ки букв в 64bit число. тем самым мы ускорим сортировку. Но это только в теории, на практике непонятно.
https://www.hindawi.com/journals/js/2018/6908760/

# Performance test results
For tests we are going to use [Calgary group dataset](http://www.data-compression.info/Corpora/CalgaryCorpus/)

## Entropy for files
| File name   | H(X)        | H(X \| X)   | H(X \| XX)  | 
| ----------- | ----------- | ----------- | ----------- |
| bib         | 5.2007      | 3.3641      | 2.3075      |
| book1       | 4.5271      | 3.5845      | 2.8141      |
| book2       | 4.7926      | 3.7452      | 2.7357      |
| geo         | 5.6464      | 4.2642      | 3.4578      |
| news        | 5.1896      | 4.0919      | 2.9228      |
| obj1        | 5.9482      | 3.4636      | 1.4005      |
| obj2        | 6.2604      | 3.8704      | 2.2654      |
| paper1      | 4.9830      | 3.6461      | 2.3318      |
| paper2      | 4.6014      | 3.5224      | 2.5136      |
| pic         | 1.2102      | 0.8237      | 0.7052      |
| progc       | 5.1990      | 3.6034      | 2.1340      |
| progl       | 4.7701      | 3.2116      | 2.0435      |
| progp       | 4.8688      | 3.1875      | 1.7551      |
| trans       | 5.5328      | 3.3548      | 1.9305      |

## A0
| File name | Raw size(bytes) | Compressed size(bytes) | Bits per byte | Elapsed time(ms) |
| ----------- | ----------- | ----------- | ----------- | ----------- |
| bib | 111261 | 72789 | 5.234 | 98 |
| book1 | 768771 | 436883 | 4.546 | 858 |
| book2 | 610856 | 364720 | 4.777 | 517 |
| geo | 102400 | 72400 | 5.656 | 244 |
| news | 377109 | 244471 | 5.186 | 580 |
| obj1 | 21504 | 16038 | 5.967 | 25 |
| obj2 | 246814 | 187294 | 6.071 | 465 |
| paper1 | 53161 | 33120 | 4.984 | 51 |
| paper2 | 82199 | 47534 | 4.626 | 109 |
| pic | 513216 | 74804 | 1.166 | 150 |
| progc | 39611 | 25920 | 5.235 | 37 |
| progl | 71646 | 42619 | 4.759 | 145 |
| progp | 49379 | 30209 | 4.894 | 73 |
| trans | 93695 | 64326 | 5.492 | 168 |
| total | 3141622 | 1713127 | 68.593 | 3520 |

## BWT <-> MTF <-> A0
| File name | Raw size(bytes) | Compressed size(bytes) | Bits per byte | Elapsed time(ms) |
| ----------- | ----------- | ----------- | ----------- | ----------- |
| bib | 111261 | 31973 | 2.299 | 153 |
| book1 | 768771 | 278141 | 2.894 | 1078 |
| book2 | 610856 | 189961 | 2.488 | 678 |
| geo | 102400 | 67008 | 5.235 | 185 |
| news | 377109 | 137605 | 2.919 | 1086 |
| obj1 | 21504 | 11584 | 4.310 | 41 |
| obj2 | 246814 | 85843 | 2.782 | 361 |
| paper1 | 53161 | 17975 | 2.705 | 32 |
| paper2 | 82199 | 27563 | 2.683 | 50 |
| pic | 513216 | 63032 | 0.983 | 1054 |
| progc | 39611 | 13542 | 2.735 | 25 |
| progl | 71646 | 17283 | 1.930 | 424 |
| progp | 49379 | 11751 | 1.904 | 33 |
| trans | 93695 | 19430 | 1.659 | 247 |
| total | 3141622 | 972691 | 37.525 | 5447 |

## RLE <-> BWT <-> MTF <-> A0
| File name | Raw size(bytes) | Compressed size(bytes) | Bits per byte | Elapsed time(ms) |
| ----------- | ----------- | ----------- | ----------- | ----------- |
| bib | 111261 | 32231 | 2.318 | 70 |
| book1 | 768771 | 281154 | 2.926 | 815 |
| book2 | 610856 | 192452 | 2.520 | 699 |
| geo | 102400 | 67062 | 5.239 | 108 |
| news | 377109 | 138575 | 2.940 | 444 |
| obj1 | 21504 | 11362 | 4.227 | 23 |
| obj2 | 246814 | 86322 | 2.798 | 387 |
| paper1 | 53161 | 18157 | 2.732 | 60 |
| paper2 | 82199 | 27854 | 2.711 | 240 |
| pic | 513216 | 53925 | 0.841 | 304 |
| progc | 39611 | 13784 | 2.784 | 86 |
| progl | 71646 | 17513 | 1.956 | 53 |
| progp | 49379 | 11826 | 1.916 | 44 |
| trans | 93695 | 19638 | 1.677 | 57 |
| total | 3141622 | 971855 | 37.583 | 3390 |

## BWT <-> MTF <-> RLE <-> A0
| File name | Raw size(bytes) | Compressed size(bytes) | Bits per byte | Elapsed time(ms) |
| ----------- | ----------- | ----------- | ----------- | ----------- |
| bib | 111261 | 29385 | 2.113 | 71 |
| book1 | 768771 | 274630 | 2.858 | 962 |
| book2 | 610856 | 184442 | 2.416 | 729 |
| geo | 102400 | 62500 | 4.883 | 137 |
| news | 377109 | 133320 | 2.828 | 461 |
| obj1 | 21504 | 10833 | 4.030 | 44 |
| obj2 | 246814 | 81723 | 2.649 | 320 |
| paper1 | 53161 | 17612 | 2.650 | 44 |
| paper2 | 82199 | 26841 | 2.612 | 50 |
| pic | 513216 | 54754 | 0.854 | 1065 |
| progc | 39611 | 13227 | 2.671 | 26 |
| progl | 71646 | 16846 | 1.881 | 45 |
| progp | 49379 | 11518 | 1.866 | 69 |
| trans | 93695 | 19153 | 1.635 | 108 |
| total | 3141622 | 936784 | 35.946 | 4131 |

## RLE <-> BWT <-> MTF <-> RLE <-> A0
| File name | Raw size(bytes) | Compressed size(bytes) | Bits per byte | Elapsed time(ms) |
| ----------- | ----------- | ----------- | ----------- | ----------- |
| bib | 111261 | 29561 | 2.126 | 70 |
| book1 | 768771 | 275895 | 2.871 | 854 |
| book2 | 610856 | 185699 | 2.432 | 727 |
| geo | 102400 | 62644 | 4.894 | 109 |
| news | 377109 | 134007 | 2.843 | 462 |
| obj1 | 21504 | 10869 | 4.044 | 19 |
| obj2 | 246814 | 82106 | 2.661 | 449 |
| paper1 | 53161 | 17716 | 2.666 | 88 |
| paper2 | 82199 | 26947 | 2.623 | 123 |
| pic | 513216 | 51040 | 0.796 | 175 |
| progc | 39611 | 13304 | 2.687 | 61 |
| progl | 71646 | 16683 | 1.863 | 44 |
| progp | 49379 | 11399 | 1.847 | 67 |
| trans | 93695 | 19292 | 1.647 | 58 |
| total | 3141622 | 937162 | 35.998 | 3306 |

## Overall result
| Type | Total compressed(bytes) | Total bits per byte | Total elapsed time(ms) | (raw - compressed)/elapsed time ratio |
| ----------- | ----------- | ----------- | ----------- | ----------- |
| A0 | 1713127 | 68.593 | 3520 | 405 |
| BWT <-> MTF <-> A0 | 972691 | 37.525 | 5447 | 398 |
| RLE <-> BWT <-> MTF <-> A0 | 971855 | 37.583 | 3390 | 640 |
| BWT <-> MTF <-> RLE <-> A0 | 936784 | 35.946 | 4131 | 533 |
| RLE <-> BWT <-> MTF <-> RLE <-> A0 | 937162 | 35.998 | 3306 | 666 |