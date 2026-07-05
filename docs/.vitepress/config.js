module.exports = {
  title: 'AI Style Studio',
  description: 'AI-powered face shape analysis and personalized style styling guidelines.',
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Guide', link: '/guide' },
      { text: 'API', link: '/api' }
    ],
    sidebar: [
      {
        text: 'Introduction',
        items: [
          { text: 'What is AI Style Studio?', link: '/' },
          { text: 'Getting Started', link: '/guide' }
        ]
      },
      {
        text: 'Core Modules',
        items: [
          { text: 'Face Shape Detector', link: '/detector' },
          { text: 'Style Mapping Engine', link: '/api' }
        ]
      }
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com' }
    ]
  }
}
