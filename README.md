# Назначение.  
Программа необходима для анализа логов.  

## 1. Проблема.  
Имеется набор id каких то сущностей. Необходимо получить дополнительную информацию по их обработке из логов.
Набор id большой, логи также большого размера.

## 2. How to use (на реальном примере)  
Рассмотрим на примере, из реальной жизни.

### 2. 1 Постановка проблемы.  
В БД видно, что некоторый набор 'платежей' завис, находится в статусе RUNNING долгое время.  
Мы получаем набор id таких 'платежей' (около 200) и логи (файлы большого размера достигают размера в 2 ГБ).

### 2.2 Цель.  
- Необходимо определить почему зависли заявки.  
- Классифицировать их по типам причин зависания, если причины разные.  
- оформить в отчет.

### 2.3 Решение.  
2.3.1 Определяем по логам текстовки, в которых логируются id 'платежей' (также можно опираться на java  код, если он есть).
Выбираем интересующие текстовки, которые могут дать какую то информацию (старт обработки, какие то промежуточные стадии, финиш обработки).  
```prose
2017-08-03 08:37:57,185 [sk-scheduler-10] DEBUG .s.PaymentStatusService - Got 27 payments from db to check, id = '112'
2017-08-03 08:37:57,217 [sk-scheduler-10] DEBUG  .s.PaymentStatusService - << checkPaymentStatus finished at Sun Aug 20 08:37:57 MSK 2017, id = '112'  
```

2.3.2 Преобразовываем их к паттернам, в которых обязательно должна быть одна единственная группировка на месте id (заключено в круглые скобки).    
```prose
.*Got \d+ payments from db to check, id = '(\d+)'  
.*<< checkPaymentStatus finished at .*, id = '(\d+)'  
```

2.3.3 Формируем файл с параметрами программы, следующего формата:  
```prose
<количество паттернов, например N>
<паттерн 1>
...
<паттерн N>
<id 1 (необязательно)>
...
<id M (необязательно)>
```
Например, params2.txt:  
```prose
2
.*Got \d+ payments from db to check, id = '(\d+)'
.*<< checkPaymentStatus finished at .*, id = '(\d+)'
112
113
114
115
116
117
118
119
```
2.3.4 Собираем jar файл.  
В корне проекта ${project.dir} выполнить:  
```programming
mvn package
```
2.3.5 Запускаем jar.  
2.3.5.1 Запускаем jar без maven  
a) Перейти в ${project.dir}/target:
```programming
cd ./target
```
b) Запустить jar файл:
```programming
java -jar big-log-analyzer-1.0.0.jar <путь к файлу лога> <путь к файлу параметров>
```
Пример:
```programming
java -jar big-log-analyzer-1.0.0.jar C:\tmp\47\application.2017-07-26.1.log C:\tmp\47\params2.txt
```
2.3.5.2 Запускаем jar с помощью maven.  
В корне проекта ${project.dir} выполнить:
```programming
mvn exec:java -Dexec.args="<путь к файлу лога> <путь к файлу параметров>"
```
Пример:
```programming
mvn exec:java -Dexec.args="C:\tmp\47\application.2017-07-26.1.log C:\tmp\47\params2.txt"
```
2.3.6 Описание вывода результата.  
Получаем 2 результата:  
FULL stat - по всем id которые нашлись в логах и соответствовали паттерну.  
STANDART stat - отфильтровано по заданным id. Если не указываем, то данная статистика не выводится.  
  
Пример результата:  
```prose
2017-08-21 00:54:34,974 [main           ] DEBUG c.k.t.a.LogAnalyzer            - ----> 5%
2017-08-21 00:54:34,974 [main           ] DEBUG c.k.t.a.LogAnalyzer            - --------> 10%
..............SKIP................
2017-08-21 00:54:34,989 [main           ] DEBUG c.k.t.a.LogAnalyzer            - -----------------------------------------------------------------------------------------------> 95%
2017-08-21 00:54:34,990 [main           ] DEBUG c.k.t.a.LogAnalyzer            - ----------------------------------------------------------------------------------------------------> 100%
2017-08-21 00:54:34,991 [main           ] INFO  c.k.t.a.LogAnalyzer            - FULL stat (all found ids, not only in parameters` file)
*** Pattern Name: .*Got \d+ payments from db to check, id = '(\d+)' ***
112	2
113	1
114	1
115	1
117	1
118	1
119	1
*** Pattern Name: .*<< checkPaymentStatus finished at .*, id = '(\d+)' ***
112	1
113	0
114	1
115	1
117	1
118	1
119	1
```

Столбец 1 показывает id 'платежей',  
столбец 2 показывает количество совпадений для данного паттерна.

2.3.7 Обработка результата.  
Далее если имеются подозрительные ситуации в статистике, нужно проводить детальный анализ.  
И классифицировать зависания платежей на соответствующие причины.

В моем примере, были 2 причины зависаний:  
a) задвоение 'платежа' с одним id (112). Быстрое нажатие на форме кнопки отправки платежа на обработку, приводило к запуску обработки платежа 2 раза подряд. В итоге второй запрос повторно выставлял платежу статус RUNNING. Но не завершался, т.к. внешняя система  не принимала дубли (внешняя система находится посередине обработки запроса).  
b) обработка 'платежа' начата, но прервалась (113). Произошла ошибка на внешней системе, результат окончания обработки не вернулся.

## 3 Особенности работы программы.  
### 3.1 Потоковое чтение файла лога.
Т.к. файлы логов могут быть большими, обработки файла производится потоковым чтением (для избежания OutOfMemoryError).  
  
### 3.2 Cкорость обработки данных.
Файл лога размером 1GB обрабатывается в среднем за 35 сек.  
Характеристики ПК:  
Процессор: Intel Core i5-6200U 2.3GHz 
ОЗУ: 16 GB  
HardDisk: SATA III  
ОС: Windows 7 Professional
