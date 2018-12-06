use nosql;

db.words_stats.insert([
    { _id: "feminism", years: [0, 0, 1, 2, 2] },
    { _id: "movements", years: [2, 2, 2, 2, 2] }
]);

id = 0;

db.texts.aggregate([
    { $match: { _id: id } },
    { $project: { words: { $split: ["$text", " "] } } },
    { $unwind : "$words" },
    { $project: {_id: 0, w: "$words"} },
    { $out : "book_splited_text" }
]);

var mapFunction = function() {
    var word = this.w;
    word = word.toLowerCase().replace(/[^a-z\-\x27]/g, "");
    emit(word, 1);
};

var reduceFunction = function(key, values) {
    return values.length;
};

db.book_splited_text.mapReduce( mapFunction, reduceFunction, "book_unique_words" );

db.book_unique_words.aggregate( [
    { $lookup: {
            from: "words_stats",
            localField: "_id",
            foreignField: "_id",
            as: "stats"
        }
    },
    { $match: { stats: {$ne: []} } },
    { $project: {
            _id:  "$_id",
            count: "$value",
            years: { $arrayElemAt: ["$stats.years", 0] }
        }
    },
    { $out : "book_unique_words_with_stats" }
]);

var words_count = db.book_splited_text.count();
var unique_words_count = db.book_unique_words.count();

var mapFunction = function() {
    emit(1, this.years.map(x => this.count*x));
};

var reduceFunction = function(key, values) {
    var years = values.reduce(
        (acc, current) =>
            acc.map( (x, i) => x + current[i] )
    );
    var max_year = Math.max(...years);

    var normalized_years = years.map(x => Math.floor(255*x/max_year));
    var lexicon_rarity = years.reduce((a, b) => a + b, 0) / words_count;

    return {lexicon_years: normalized_years, lexicon_rarity: lexicon_rarity};
};

var res = db.runCommand(
    {
        mapReduce: "book_unique_words_with_stats",
        map: mapFunction,
        reduce: reduceFunction,
        out: { inline: 1 },
        scope: {words_count: words_count}
    }
)["results"][0]["value"];

var lexicon_years = res.lexicon_years;
var lexicon_rarity = res.lexicon_rarity;

var stemmer = function(w) {
    var step2list = {
            "ational" : "ate",
            "tional" : "tion",
            "enci" : "ence",
            "anci" : "ance",
            "izer" : "ize",
            "bli" : "ble",
            "alli" : "al",
            "entli" : "ent",
            "eli" : "e",
            "ousli" : "ous",
            "ization" : "ize",
            "ation" : "ate",
            "ator" : "ate",
            "alism" : "al",
            "iveness" : "ive",
            "fulness" : "ful",
            "ousness" : "ous",
            "aliti" : "al",
            "iviti" : "ive",
            "biliti" : "ble",
            "logi" : "log"
        },

        step3list = {
            "icate" : "ic",
            "ative" : "",
            "alize" : "al",
            "iciti" : "ic",
            "ical" : "ic",
            "ful" : "",
            "ness" : ""
        },

        c = "[^aeiou]",          // consonant
        v = "[aeiouy]",          // vowel
        C = c + "[^aeiouy]*",    // consonant sequence
        V = v + "[aeiou]*",      // vowel sequence

        mgr0 = "^(" + C + ")?" + V + C,               // [C]VC... is m>0
        meq1 = "^(" + C + ")?" + V + C + "(" + V + ")?$",  // [C]VC[V] is m=1
        mgr1 = "^(" + C + ")?" + V + C + V + C,       // [C]VCVC... is m>1
        s_v = "^(" + C + ")?" + v;                   // vowel in stem

    var 	stem,
        suffix,
        firstch,
        re,
        re2,
        re3,
        re4,
        origword = w;

    if (w.length < 3) { return w; }

    firstch = w.substr(0,1);
    if (firstch == "y") {
        w = firstch.toUpperCase() + w.substr(1);
    }

    // Step 1a
    re = /^(.+?)(ss|i)es$/;
    re2 = /^(.+?)([^s])s$/;

    if (re.test(w)) { w = w.replace(re,"$1$2"); }
    else if (re2.test(w)) {	w = w.replace(re2,"$1$2"); }

    // Step 1b
    re = /^(.+?)eed$/;
    re2 = /^(.+?)(ed|ing)$/;
    if (re.test(w)) {
        var fp = re.exec(w);
        re = new RegExp(mgr0);
        if (re.test(fp[1])) {
            re = /.$/;
            w = w.replace(re,"");
        }
    } else if (re2.test(w)) {
        var fp = re2.exec(w);
        stem = fp[1];
        re2 = new RegExp(s_v);
        if (re2.test(stem)) {
            w = stem;
            re2 = /(at|bl|iz)$/;
            re3 = new RegExp("([^aeiouylsz])\\1$");
            re4 = new RegExp("^" + C + v + "[^aeiouwxy]$");
            if (re2.test(w)) {	w = w + "e"; }
            else if (re3.test(w)) { re = /.$/; w = w.replace(re,""); }
            else if (re4.test(w)) { w = w + "e"; }
        }
    }

    // Step 1c
    re = /^(.+?)y$/;
    if (re.test(w)) {
        var fp = re.exec(w);
        stem = fp[1];
        re = new RegExp(s_v);
        if (re.test(stem)) { w = stem + "i"; }
    }

    // Step 2
    re = /^(.+?)(ational|tional|enci|anci|izer|bli|alli|entli|eli|ousli|ization|ation|ator|alism|iveness|fulness|ousness|aliti|iviti|biliti|logi)$/;
    if (re.test(w)) {
        var fp = re.exec(w);
        stem = fp[1];
        suffix = fp[2];
        re = new RegExp(mgr0);
        if (re.test(stem)) {
            w = stem + step2list[suffix];
        }
    }

    // Step 3
    re = /^(.+?)(icate|ative|alize|iciti|ical|ful|ness)$/;
    if (re.test(w)) {
        var fp = re.exec(w);
        stem = fp[1];
        suffix = fp[2];
        re = new RegExp(mgr0);
        if (re.test(stem)) {
            w = stem + step3list[suffix];
        }
    }

    // Step 4
    re = /^(.+?)(al|ance|ence|er|ic|able|ible|ant|ement|ment|ent|ou|ism|ate|iti|ous|ive|ize)$/;
    re2 = /^(.+?)(s|t)(ion)$/;
    if (re.test(w)) {
        var fp = re.exec(w);
        stem = fp[1];
        re = new RegExp(mgr1);
        if (re.test(stem)) {
            w = stem;
        }
    } else if (re2.test(w)) {
        var fp = re2.exec(w);
        stem = fp[1] + fp[2];
        re2 = new RegExp(mgr1);
        if (re2.test(stem)) {
            w = stem;
        }
    }

    // Step 5
    re = /^(.+?)e$/;
    if (re.test(w)) {
        var fp = re.exec(w);
        stem = fp[1];
        re = new RegExp(mgr1);
        re2 = new RegExp(meq1);
        re3 = new RegExp("^" + C + v + "[^aeiouwxy]$");
        if (re.test(stem) || (re2.test(stem) && !(re3.test(stem)))) {
            w = stem;
        }
    }

    re = /ll$/;
    re2 = new RegExp(mgr1);
    if (re.test(w) && re2.test(w)) {
        re = /.$/;
        w = w.replace(re,"");
    }

    // and turn initial Y back to y

    if (firstch == "y") {
        w = firstch.toLowerCase() + w.substr(1);
    }

    return w;
};


var mapFunction = function() {
    emit(stem(this._id), this.value);
};

var reduceFunction = function(key, values) {
    return values.reduce((a, b) => a + b, 0);
};

db.runCommand(
    {
        mapReduce: "book_unique_words",
        map: mapFunction,
        reduce: reduceFunction,
        out: "book_stemmed_words",
        scope: {stem: stemmer}
    }
);

var unique_stems_count = db.book_stemmed_words.count();


var computeDifficulty = function(words_count, unique_words_count, unique_stems_count, lexicon_years, lexicon_rarity) {
    return unique_stems_count / words_count;
};

id = db.books_stats.insertOne(
    {
        words_count: words_count,
        unique_words_count: unique_words_count,
        unique_stems_count: unique_stems_count,

        lexicon_years: lexicon_years,
        lexicon_rarity: lexicon_rarity,

        difficulty: computeDifficulty(words_count, unique_words_count, unique_stems_count, lexicon_years, lexicon_rarity)
    }
).insertedId;

db.texts.drop();
db.book_splited_text.drop();
db.book_unique_words.drop();
db.book_unique_words_with_stats.drop();
db.book_stemmed_words.drop();

id