const index = require('./agent')
/*
sample file
*/

test('1 + 2 = 3', () => {
   expect(index.sum(1,2)).toEqual(3)
})

test('10 / 3 ~= 3.33', () => {
   expect(index.division(10, 3)).toBeCloseTo(3.33)
})
