import React from 'react';

export const Navbar = () => {
  return (
    <nav className="fixed w-full z-50 bg-white border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex-shrink-0 font-bold text-2xl text-indigo-600">WorkSuite</div>
          <div className="hidden md:flex space-x-8">
            {['Home', 'Product', 'Solutions', 'Features', 'About Us'].map((item) => (
              <a key={item} href={`#${item.toLowerCase().replace(' ', '-')}`} className="text-gray-600 hover:text-indigo-600">
                {item}
              </a>
            ))}
          </div>
          <div className="flex items-center space-x-4">
            <a href="/login" className="text-gray-600 font-medium">Login</a>
            <a href="/get-started" className="bg-indigo-600 text-white px-4 py-2 rounded-lg font-medium">Get Started</a>
          </div>
        </div>
      </div>
    </nav>
  );
};
