const FIXTURES = [
  {
    title: '两数相加',
    statement: '读取两个整数，输出它们的和。',
    inputDescription: '一行两个整数 a b。',
    outputDescription: '输出 a+b。',
    input: '2 3\n', expectedOutput: '5',
    sourceCode: 'a, b = map(int, input().split())\nprint(a + b)\n',
  },
  {
    title: '数组求和',
    statement: '读取整数数组，输出所有元素之和。',
    inputDescription: '第一行为 n，第二行为 n 个整数。',
    outputDescription: '输出数组元素之和。',
    input: '5\n1 2 3 4 5\n', expectedOutput: '15',
    sourceCode: 'n = int(input())\nvalues = list(map(int, input().split()))\nprint(sum(values[:n]))\n',
  },
  {
    title: '数组最大值',
    statement: '读取整数数组，输出最大元素。',
    inputDescription: '第一行为 n，第二行为 n 个整数。',
    outputDescription: '输出数组最大值。',
    input: '5\n3 1 9 2 7\n', expectedOutput: '9',
    sourceCode: 'n = int(input())\nvalues = list(map(int, input().split()))\nprint(max(values[:n]))\n',
  },
  {
    title: '闭区间求和',
    statement: '读取区间端点，输出闭区间内所有整数之和。',
    inputDescription: '一行两个整数 left right。',
    outputDescription: '输出区间元素之和。',
    input: '3 7\n', expectedOutput: '25',
    sourceCode: 'left, right = map(int, input().split())\nprint(sum(range(left, right + 1)))\n',
  },
  {
    title: '最大公约数',
    statement: '读取两个正整数，输出最大公约数。',
    inputDescription: '一行两个正整数 a b。',
    outputDescription: '输出 gcd(a,b)。',
    input: '48 18\n', expectedOutput: '6',
    sourceCode: 'import math\na, b = map(int, input().split())\nprint(math.gcd(a, b))\n',
  },
  {
    title: '素数判断',
    statement: '判断给定整数是否为素数。',
    inputDescription: '一行一个整数 n。',
    outputDescription: '素数输出 YES，否则输出 NO。',
    input: '29\n', expectedOutput: 'YES',
    sourceCode: 'import math\nn = int(input())\nprime = n >= 2 and all(n % d for d in range(2, math.isqrt(n) + 1))\nprint("YES" if prime else "NO")\n',
  },
  {
    title: '斐波那契数列',
    statement: '输出第 n 个斐波那契数，F(0)=0，F(1)=1。',
    inputDescription: '一行一个非负整数 n。',
    outputDescription: '输出 F(n)。',
    input: '10\n', expectedOutput: '55',
    sourceCode: 'n = int(input())\na, b = 0, 1\nfor _ in range(n):\n    a, b = b, a + b\nprint(a)\n',
  },
  {
    title: '阶乘',
    statement: '读取非负整数 n，输出 n 的阶乘。',
    inputDescription: '一行一个非负整数 n。',
    outputDescription: '输出 n!。',
    input: '6\n', expectedOutput: '720',
    sourceCode: 'import math\nn = int(input())\nprint(math.factorial(n))\n',
  },
  {
    title: '字符串反转',
    statement: '读取不包含空白的字符串并反转。',
    inputDescription: '一行一个字符串。',
    outputDescription: '输出反转后的字符串。',
    input: 'bluenet\n', expectedOutput: 'teneulb',
    sourceCode: 'value = input().strip()\nprint(value[::-1])\n',
  },
  {
    title: '回文判断',
    statement: '判断给定字符串是否为回文。',
    inputDescription: '一行一个字符串。',
    outputDescription: '回文输出 YES，否则输出 NO。',
    input: 'level\n', expectedOutput: 'YES',
    sourceCode: 'value = input().strip()\nprint("YES" if value == value[::-1] else "NO")\n',
  },
  {
    title: '字符出现次数',
    statement: '统计目标字符在字符串中出现的次数。',
    inputDescription: '一行字符串和目标字符，以空格分隔。',
    outputDescription: '输出出现次数。',
    input: 'banana a\n', expectedOutput: '3',
    sourceCode: 'value, target = input().split()\nprint(value.count(target))\n',
  },
  {
    title: '数组排序',
    statement: '将整数数组升序排序。',
    inputDescription: '第一行为 n，第二行为 n 个整数。',
    outputDescription: '输出排序后的数组。',
    input: '5\n3 1 2 5 4\n', expectedOutput: '1 2 3 4 5',
    sourceCode: 'n = int(input())\nvalues = list(map(int, input().split()))\nprint(*sorted(values[:n]))\n',
  },
  {
    title: '二分查找',
    statement: '在升序数组中查找目标值，返回从零开始的下标。',
    inputDescription: '第一行为 n 和目标值，第二行为升序数组。',
    outputDescription: '输出目标下标，不存在输出 -1。',
    input: '5 4\n1 2 3 4 5\n', expectedOutput: '3',
    sourceCode: 'import bisect\nn, target = map(int, input().split())\nvalues = list(map(int, input().split()))[:n]\npos = bisect.bisect_left(values, target)\nprint(pos if pos < n and values[pos] == target else -1)\n',
  },
  {
    title: '不同元素计数',
    statement: '统计数组中不同整数的数量。',
    inputDescription: '第一行为 n，第二行为 n 个整数。',
    outputDescription: '输出不同元素数量。',
    input: '7\n1 2 1 3 2 4 4\n', expectedOutput: '4',
    sourceCode: 'n = int(input())\nvalues = list(map(int, input().split()))\nprint(len(set(values[:n])))\n',
  },
  {
    title: '矩阵元素求和',
    statement: '读取矩阵并输出全部元素之和。',
    inputDescription: '第一行为行数和列数，随后为矩阵元素。',
    outputDescription: '输出矩阵元素之和。',
    input: '2 3\n1 2 3\n4 5 6\n', expectedOutput: '21',
    sourceCode: 'rows, cols = map(int, input().split())\ntotal = 0\nfor _ in range(rows):\n    total += sum(list(map(int, input().split()))[:cols])\nprint(total)\n',
  },
].map((fixture, index) => ({
  ...fixture,
  questionIndex: index + 1,
  constraints: '输入规模限定在本地压测轻量范围内。',
  generatorSource: `print(${JSON.stringify(fixture.input)})`,
}));

/** 返回 15 种不同算法中的一道题；序号从 1 开始。 */
export function algorithmFixture(index) {
  if (!Number.isInteger(index) || index < 1 || index > FIXTURES.length) {
    throw new Error(`algorithm fixture index must be between 1 and ${FIXTURES.length}`);
  }
  return FIXTURES[index - 1];
}

export const ALGORITHM_FIXTURE_COUNT = FIXTURES.length;
