/**
 * IMPORTS
 */

const h = require('./helpers')
const p = require('./parameters')

/**
 * DF FUNCTIONS
 */

/* RETRIEVE DATA FROM DIRECTORY FACILITATOR */
function getDF() {
  h.get('./services').then(function(dfstr) {
    df = JSON.parse(dfstr);
    if (Object.keys(df).length > 0) {
      var table = h.createTable("dfsection");
      Object.keys(df).forEach(function(a) {
        df[a].services.sort().forEach(function(s) {
          h.addTwoCellsInARow(table, df[a].agent, s);
        });
      });
    } else {
      var table = h.createTable("dfsection");
      h.addTwoCellsInARow(table, "nothing to show", "");
    }
  });
}

/**
 * EXPORTS
 */

window.getDF = getDF;

/**
 * END OF FILE
 */
