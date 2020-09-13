package com.example.easynews.`interface`

import com.example.easynews.model.News
import retrofit2.Call
import retrofit2.http.*


interface NewsService {

    @Headers("X-Api-Key: 848c158751374e079db06e2de9191ad5")
    @GET("v2/everything?sortBy=publishedAt&pageSize=100&" +
            "domains=$domains")
    fun getLocalNews(@Query("q") cityName: String, @Query("language") lang: String): Call<News>

    @Headers("X-Api-Key: 848c158751374e079db06e2de9191ad5")
    @GET("v2/top-headlines?pageSize=100")
    fun getGlobalNews(@Query("country") country: String): Call<News>

    @Headers("X-Api-Key: 848c158751374e079db06e2de9191ad5")
    @GET("v2/everything?sortBy=publishedAt&pageSize=100")
    fun getNewsFromSearch(@Query("q") searchValue: String): Call<News>

    companion object {
        const val domains: String  = "paperpaper.ru, vesti.ru, life.ru, fontanka.ru, www.rbc.ru, lenta.ru, russian.rt.com, izvestia.ru, vz.ru, mk.ru, dni.ru, " +
                "newsru.com, meduza.io, ria.ru, vedomosti.ru, gazeta.ru, topnews.ru, rg.ru, kp.ru, regnum.ru, m24.ru, " +
                "forbes.ru, bbc.co.uk, abcnews.go.com, abc.net.au, aftenposten.no, aljazeera.com, ansa.it, argaam.com, arstechnica.com, " +
                "arynews.tv, apnews.com, afr.com, axios.com, bild.de, br.blastingnews.com, bleacherreport.com, bloomberg.com, businessinsider.com, " +
                "uk.businessinsider.com, buzzfeed.com, cbc.ca, cbsnews.com, us.cnn.com, cnnespanol.cnn.com, ccn.com, zeit.de, elmundo.es, " +
                "ew.com, espn.go.com, espncricinfo.com, business.financialpost.com, focus.de, football-italia.net, fortune.com, fourfourtwo.com, " +
                "foxnews.com, foxsports.com, globo.com, news.google.com, gp.se, gruenderszene.de, news.ycombinator.com, handelsblatt.com, " +
                "ign.com, ilsole24ore.com, independent.co.uk, infobae.com, infomoney.com.br, lagaceta.com.ar, lanacion.com.ar, repubblica.it, " +
                "lemonde.fr, lequipe.fr, lesechos.fr, liberation.fr, marca.com, mashable.com, medicalnewstoday.com, msnbc.com, mtv.com, " +
                "mtv.co.uk, news.nationalgeographic.com, nationalreview.com, nbcnews.com, news24.com, newscientist.com, news.com.au, " +
                "newsweek.com, nymag.com, nextbigfuture.com, nfl.com, nhl.com, nrk.no, politico.com, polygon.com, recode.net, reuters.com, " +
                "rte.ie, rtlnieuws.nl, sabq.org, spiegel.de, svd.se, t3n.de, talksport.com, techcrunch.com, techcrunch.cn, techradar.com, " +
                "theamericanconservative.com, theglobeandmail.com, thehill.com, thehindu.com, huffingtonpost.com, irishtimes.com, jpost.com, " +
                "theladbible.com, thenextweb.com, thesportbible.com, timesofindia.indiatimes.com, theverge.com, wsj.com, washingtonpost.com, " +
                "washingtontimes.com, time.com, usatoday.com, news.vice.com, wired.com, wired.de, wiwo.de, xinhuanet.com, ynet.co.il"
    }

}